package com.hotel.hotelservice.service;

import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomAllocation;
import com.hotel.hotelservice.entity.RoomStatus;
import com.hotel.hotelservice.repository.RoomAllocationRepository;
import com.hotel.hotelservice.repository.RoomRepository;
import com.hotel.hotelservice.service.impl.RoomAllocationServiceImpl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAllocationServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAllocationRepository roomAllocationRepository;

    @InjectMocks
    private RoomAllocationServiceImpl roomAllocationService;

    @Test
    void allocateRooms_shouldAllocateRooms_whenAvailable() {

        Long bookingId = 1L;
        Long hotelId = 10L;
        Long categoryId = 5L;

        when(roomAllocationRepository
                .findAllByBookingIdAndReleasedAtIsNull(bookingId))
                .thenReturn(List.of());

        Room room1 = Room.builder()
                .id(1L)
                .status(RoomStatus.AVAILABLE)
                .build();

        Room room2 = Room.builder()
                .id(2L)
                .status(RoomStatus.AVAILABLE)
                .build();

        when(roomRepository
                .findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                        hotelId,
                        categoryId,
                        RoomStatus.AVAILABLE
                ))
                .thenReturn(List.of(room1, room2));

        roomAllocationService.allocateRooms(
                bookingId,
                hotelId,
                categoryId,
                2
        );
        verify(roomAllocationRepository, times(2))
                .save(any(RoomAllocation.class));
        verify(roomRepository, times(2))
                .save(any(Room.class));

        assertThat(room1.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
        assertThat(room2.getStatus()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    void allocateRooms_shouldFail_whenAlreadyAllocated() {

        when(roomAllocationRepository
                .findAllByBookingIdAndReleasedAtIsNull(1L))
                .thenReturn(List.of(new RoomAllocation()));

        assertThatThrownBy(() ->
                roomAllocationService.allocateRooms(
                        1L, 10L, 5L, 1
                )
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Rooms already allocated");

        verifyNoInteractions(roomRepository);
    }

    @Test
    void allocateRooms_shouldFail_whenInsufficientRooms() {

        when(roomAllocationRepository
                .findAllByBookingIdAndReleasedAtIsNull(1L))
                .thenReturn(List.of());

        when(roomRepository
                .findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                        10L, 5L, RoomStatus.AVAILABLE
                ))
                .thenReturn(List.of(
                        Room.builder().id(1L).status(RoomStatus.AVAILABLE).build()
                ));

        assertThatThrownBy(() ->
                roomAllocationService.allocateRooms(
                        1L, 10L, 5L, 2
                )
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Not enough available rooms");

        verify(roomAllocationRepository, never()).save(any());
    }

    @Test
    void releaseRooms_shouldReleaseAndUpdateStatus() {

        RoomAllocation allocation1 = RoomAllocation.builder()
                .roomId(1L)
                .allocatedAt(LocalDateTime.now())
                .build();

        RoomAllocation allocation2 = RoomAllocation.builder()
                .roomId(2L)
                .allocatedAt(LocalDateTime.now())
                .build();

        when(roomAllocationRepository
                .findAllByBookingIdAndReleasedAtIsNull(1L))
                .thenReturn(List.of(allocation1, allocation2));

        Room room1 = Room.builder()
                .id(1L)
                .status(RoomStatus.OCCUPIED)
                .build();

        Room room2 = Room.builder()
                .id(2L)
                .status(RoomStatus.OCCUPIED)
                .build();

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room1));

        when(roomRepository.findById(2L))
                .thenReturn(Optional.of(room2));

        roomAllocationService.releaseRooms(1L);

        assertThat(allocation1.getReleasedAt()).isNotNull();
        assertThat(allocation2.getReleasedAt()).isNotNull();

        assertThat(room1.getStatus()).isEqualTo(RoomStatus.CLEANING);
        assertThat(room2.getStatus()).isEqualTo(RoomStatus.CLEANING);

        verify(roomAllocationRepository, times(2))
                .save(any(RoomAllocation.class));

        verify(roomRepository, times(2))
                .save(any(Room.class));
    }

    @Test
    void releaseRooms_shouldFail_whenNoAllocations() {

        when(roomAllocationRepository
                .findAllByBookingIdAndReleasedAtIsNull(1L))
                .thenReturn(List.of());

        assertThatThrownBy(() ->
                roomAllocationService.releaseRooms(1L)
        ).isInstanceOf(EntityNotFoundException.class)
         .hasMessageContaining("No active room allocations");

        verifyNoInteractions(roomRepository);
    }

    @Test
    void releaseRooms_shouldFail_whenRoomMissing() {

        RoomAllocation allocation = RoomAllocation.builder()
                .roomId(99L)
                .build();

        when(roomAllocationRepository
                .findAllByBookingIdAndReleasedAtIsNull(1L))
                .thenReturn(List.of(allocation));

        when(roomRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roomAllocationService.releaseRooms(1L)
        ).isInstanceOf(EntityNotFoundException.class)
         .hasMessageContaining("Room not found");

        verify(roomRepository).findById(99L);
    }
}
