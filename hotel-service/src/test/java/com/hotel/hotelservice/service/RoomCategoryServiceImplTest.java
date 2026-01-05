package com.hotel.hotelservice.service;

import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.response.RoomCategoryResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.RoomCategory;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.RoomCategoryRepository;
import com.hotel.hotelservice.service.impl.RoomCategoryServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomCategoryServiceImplTest {

    @Mock
    private RoomCategoryRepository roomCategoryRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomCategoryServiceImpl roomCategoryService;
    @Test
    void addCategory_shouldCreateCategory_whenAdmin() {

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setCategory("deluxe");
        request.setTotalRooms(10);
        request.setCapacity(2);
        request.setBasePrice(2500.0);

        Hotel hotel = Hotel.builder()
                .id(1L)
                .managerEmail("manager@test.com")
                .build();

        when(roomCategoryRepository
                .existsByHotelIdAndCategoryIgnoreCase(1L, "deluxe"))
                .thenReturn(false);

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        when(roomCategoryRepository.save(any(RoomCategory.class)))
                .thenAnswer(invocation -> {
                    RoomCategory rc = invocation.getArgument(0);
                    rc.setId(100L);
                    return rc;
                });

        RoomCategoryResponse response =
                roomCategoryService.addCategory(1L, request, "ADMIN");

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getCategory()).isEqualTo("DELUXE");
        assertThat(response.getTotalRooms()).isEqualTo(10);
    }

    @Test
    void addCategory_shouldThrowUnauthorized_whenNotAdmin() {

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setCategory("deluxe");

        assertThatThrownBy(() ->
                roomCategoryService.addCategory(1L, request, "MANAGER")
        ).isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(roomCategoryRepository, hotelRepository);
    }

    @Test
    void addCategory_shouldThrow_whenDuplicateCategory() {

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setCategory("deluxe");

        when(roomCategoryRepository
                .existsByHotelIdAndCategoryIgnoreCase(1L, "deluxe"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                roomCategoryService.addCategory(1L, request, "ADMIN")
        ).isInstanceOf(IllegalArgumentException.class);

        verify(hotelRepository, never()).findById(any());
    }

    @Test
    void addCategory_shouldThrow_whenHotelNotFound() {

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setCategory("deluxe");

        when(roomCategoryRepository
                .existsByHotelIdAndCategoryIgnoreCase(1L, "deluxe"))
                .thenReturn(false);

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roomCategoryService.addCategory(1L, request, "ADMIN")
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategory_shouldUpdate_whenAdmin() {

        Hotel hotel = Hotel.builder()
                .managerEmail("manager@test.com")
                .build();

        RoomCategory category = RoomCategory.builder()
                .id(10L)
                .hotel(hotel)
                .build();

        when(roomCategoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setTotalRooms(20);
        request.setCapacity(3);
        request.setBasePrice(4000.0);

        RoomCategoryResponse response =
                roomCategoryService.updateCategory(
                        10L, request, "admin@test.com", "ADMIN"
                );

        assertThat(response.getTotalRooms()).isEqualTo(20);
        assertThat(response.getCapacity()).isEqualTo(3);
        assertThat(response.getBasePrice()).isEqualTo(4000.0);
    }

    @Test
    void updateCategory_shouldThrowUnauthorized_whenManagerNotAssigned() {

        Hotel hotel = Hotel.builder()
                .managerEmail("manager@test.com")
                .build();

        RoomCategory category = RoomCategory.builder()
                .hotel(hotel)
                .build();

        when(roomCategoryRepository.findById(10L))
                .thenReturn(Optional.of(category));

        RoomCategoryRequest request = new RoomCategoryRequest();

        assertThatThrownBy(() ->
                roomCategoryService.updateCategory(
                        10L,
                        request,
                        "other@test.com",
                        "MANAGER"
                )
        ).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateCategory_shouldThrow_whenCategoryNotFound() {

        when(roomCategoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roomCategoryService.updateCategory(
                        99L,
                        new RoomCategoryRequest(),
                        "admin@test.com",
                        "ADMIN"
                )
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCategoriesByHotel_shouldReturnList() {

        RoomCategory c1 = RoomCategory.builder()
                .id(1L)
                .category("DELUXE")
                .totalRooms(10)
                .capacity(2)
                .basePrice(3000.0)
                .build();

        RoomCategory c2 = RoomCategory.builder()
                .id(2L)
                .category("STANDARD")
                .totalRooms(20)
                .capacity(2)
                .basePrice(1500.0)
                .build();

        when(roomCategoryRepository.findByHotelId(1L))
                .thenReturn(List.of(c1, c2));

        List<RoomCategoryResponse> responses =
                roomCategoryService.getCategoriesByHotel(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategory()).isEqualTo("DELUXE");
    }

    @Test
    void getCategoryById_shouldReturnCategory() {

        RoomCategory category = RoomCategory.builder()
                .id(5L)
                .category("DELUXE")
                .totalRooms(10)
                .capacity(2)
                .basePrice(3000.0)
                .build();

        when(roomCategoryRepository.findById(5L))
                .thenReturn(Optional.of(category));

        RoomCategoryResponse response =
                roomCategoryService.getCategoryById(5L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getCategory()).isEqualTo("DELUXE");
    }

    @Test
    void getCategoryById_shouldThrow_whenNotFound() {

        when(roomCategoryRepository.findById(100L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roomCategoryService.getCategoryById(100L)
        ).isInstanceOf(ResourceNotFoundException.class);
    }
}
