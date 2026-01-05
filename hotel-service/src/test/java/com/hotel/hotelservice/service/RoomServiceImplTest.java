package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.request.BulkCreateRoomRequest;
import com.hotel.hotelservice.dto.request.CreateRoomRequest;
import com.hotel.hotelservice.dto.response.RoomResponse;
import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomCategory;
import com.hotel.hotelservice.entity.RoomStatus;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.RoomCategoryRepository;
import com.hotel.hotelservice.repository.RoomRepository;
import com.hotel.hotelservice.service.impl.RoomServiceImpl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomCategoryRepository roomCategoryRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void suggestRoom_shouldReturnSuggestion_whenReceptionist() {

        Room room1 = Room.builder()
                .id(1L)
                .roomNumber("101")
                .status(RoomStatus.AVAILABLE)
                .build();

        Room room2 = Room.builder()
                .id(2L)
                .roomNumber("102")
                .status(RoomStatus.AVAILABLE)
                .build();

        when(roomRepository
                .findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                        1L, 10L, RoomStatus.AVAILABLE))
                .thenReturn(List.of(room1, room2));

        RoomSuggestionResponse response =
                roomService.suggestRoom(1L, 10L, "RECEPTIONIST");

        assertThat(response.getSuggestedRoom().getRoomNumber()).isEqualTo("101");
        assertThat(response.getAvailableRooms()).hasSize(2);
    }

    @Test
    void suggestRoom_shouldThrow_whenUnauthorizedRole() {

        assertThatThrownBy(() ->
                roomService.suggestRoom(1L, 10L, "GUEST")
        ).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void suggestRoom_shouldThrow_whenNoRoomsAvailable() {

        when(roomRepository
                .findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                        any(), any(), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                roomService.suggestRoom(1L, 10L, "MANAGER")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRoomStatus_shouldAllowCleaningToAvailable_byReceptionist() {

        Room room = Room.builder()
                .id(1L)
                .status(RoomStatus.CLEANING)
                .build();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        roomService.updateRoomStatus(1L, "AVAILABLE", "RECEPTIONIST");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.AVAILABLE);
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoomStatus_shouldThrow_whenInvalidTransition() {

        Room room = Room.builder()
                .status(RoomStatus.AVAILABLE)
                .build();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        assertThatThrownBy(() ->
                roomService.updateRoomStatus(1L, "AVAILABLE", "MANAGER")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRoomStatus_shouldAllowMaintenance_byAdmin() {

        Room room = Room.builder()
                .status(RoomStatus.AVAILABLE)
                .build();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        roomService.updateRoomStatus(1L, "MAINTENANCE", "ADMIN");

        assertThat(room.getStatus()).isEqualTo(RoomStatus.MAINTENANCE);
    }

    @Test
    void updateRoomStatus_shouldThrow_whenOccupiedToMaintenance() {

        Room room = Room.builder()
                .status(RoomStatus.OCCUPIED)
                .build();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        assertThatThrownBy(() ->
                roomService.updateRoomStatus(1L, "MAINTENANCE", "MANAGER")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateRoomStatus_shouldThrow_whenRoomNotFound() {

        when(roomRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roomService.updateRoomStatus(1L, "AVAILABLE", "ADMIN")
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRoom_shouldCreate_whenAdmin() {

        CreateRoomRequest request = new CreateRoomRequest();
        request.setCategoryId(10L);
        request.setRoomNumber("101");

        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101"))
                .thenReturn(false);

        roomService.createRoom(1L, request, "ADMIN");

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void createRoom_shouldThrow_whenDuplicateRoom() {

        CreateRoomRequest request = new CreateRoomRequest();
        request.setRoomNumber("101");

        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                roomService.createRoom(1L, request, "MANAGER")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bulkCreateRooms_shouldCreateRooms() {

        BulkCreateRoomRequest request = new BulkCreateRoomRequest();
        request.setCategoryId(10L);
        request.setStart(101);
        request.setEnd(103);

        when(roomRepository.existsByHotelIdAndRoomNumber(any(), any()))
                .thenReturn(false);

        roomService.bulkCreateRooms(1L, request, "ADMIN");

        verify(roomRepository, times(3)).save(any(Room.class));
    }

    @Test
    void bulkCreateRooms_shouldThrow_whenInvalidRange() {

        BulkCreateRoomRequest request = new BulkCreateRoomRequest();
        request.setStart(200);
        request.setEnd(100);

        assertThatThrownBy(() ->
                roomService.bulkCreateRooms(1L, request, "ADMIN")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRoomsByHotel_shouldReturnRooms_forAdmin() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .managerEmail("manager@test.com")
                .build();

        Room room = Room.builder()
                .id(1L)
                .roomNumber("101")
                .categoryId(10L)
                .status(RoomStatus.AVAILABLE)
                .build();

        RoomCategory category = RoomCategory.builder()
                .id(10L)
                .category("DELUXE")
                .build();

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        when(roomRepository.findByHotelId(1L))
                .thenReturn(List.of(room));

        when(roomCategoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        List<RoomResponse> responses =
                roomService.getRoomsByHotel(1L, "ADMIN", "x@test.com");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCategory()).isEqualTo("DELUXE");
    }

    @Test
    void getRoomsByHotel_shouldThrow_whenManagerNotAssigned() {

        Hotel hotel = Hotel.builder()
                .managerEmail("other@test.com")
                .build();

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        assertThatThrownBy(() ->
                roomService.getRoomsByHotel(1L, "MANAGER", "wrong@test.com")
        ).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getRoomsByHotel_shouldThrow_whenHotelNotFound() {

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roomService.getRoomsByHotel(1L, "ADMIN", "x@test.com")
        ).isInstanceOf(ResourceNotFoundException.class);
    }
}
