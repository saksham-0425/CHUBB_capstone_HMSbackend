package com.hotel.hotelservice.service;

import com.hotel.hotelservice.client.AuthClient;
import com.hotel.hotelservice.dto.request.CreateHotelRequest;
import com.hotel.hotelservice.dto.request.CreateManagerRequest;
import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.request.UpdateHotelRequest;
import com.hotel.hotelservice.dto.response.HotelResponse;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.HotelStaff;
import com.hotel.hotelservice.entity.Role;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.HotelStaffRepository;
import com.hotel.hotelservice.repository.RoomCategoryRepository;
import com.hotel.hotelservice.service.impl.HotelServiceImpl;

import jakarta.persistence.EntityNotFoundException;
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
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomCategoryRepository roomCategoryRepository;

    @Mock
    private AuthClient authClient;

    @Mock
    private HotelStaffRepository hotelStaffRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    @Test
    void createHotel_shouldCreateHotel_whenRoleIsAdmin() {

        RoomCategoryRequest categoryRequest = new RoomCategoryRequest();
        categoryRequest.setCategory("deluxe");
        categoryRequest.setTotalRooms(10);
        categoryRequest.setCapacity(2);
        categoryRequest.setBasePrice(3000.0);

        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("Taj")
                .city("Mumbai")
                .address("Marine Drive")
                .description("Luxury")
                .managerEmail("manager@test.com")
                .amenities(List.of("WIFI", "POOL"))
                .roomCategories(List.of(categoryRequest))
                .build();

        when(hotelRepository.save(any(Hotel.class)))
                .thenAnswer(invocation -> {
                    Hotel h = invocation.getArgument(0);
                    h.setId(1L);
                    h.getRoomCategories().get(0).setId(10L);
                    return h;
                });

        HotelResponse response = hotelService.createHotel(request, "ADMIN");

        verify(authClient).createManager(any(CreateManagerRequest.class));
        verify(hotelRepository).save(any(Hotel.class));

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmenities()).containsExactly("WIFI", "POOL");
        assertThat(response.getRoomCategories()).hasSize(1);
        assertThat(response.getRoomCategories().get(0).getCategory())
                .isEqualTo("DELUXE");
    }

    @Test
    void createHotel_shouldThrowUnauthorized_whenRoleIsNotAdmin() {

        CreateHotelRequest request = CreateHotelRequest.builder().build();

        assertThatThrownBy(() ->
                hotelService.createHotel(request, "MANAGER")
        ).isInstanceOf(UnauthorizedException.class);

        verifyNoInteractions(authClient, hotelRepository);
    }

    @Test
    void updateHotel_shouldUpdate_whenAdmin() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Old Name")
                .managerEmail("manager@test.com")
                .roomCategories(List.of())
                .amenities("WIFI")
                .build();

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        UpdateHotelRequest request = UpdateHotelRequest.builder()
                .name("New Name")
                .amenities(List.of("WIFI", "POOL"))
                .build();

        HotelResponse response =
                hotelService.updateHotel(1L, request, "admin@test.com", "ADMIN");

        assertThat(response.getName()).isEqualTo("New Name");
        assertThat(response.getAmenities()).contains("POOL");
    }

    @Test
    void updateHotel_shouldThrowUnauthorized_whenManagerNotAssigned() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .managerEmail("manager@test.com")
                .build();

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        UpdateHotelRequest request = UpdateHotelRequest.builder()
                .name("Hack")
                .build();

        assertThatThrownBy(() ->
                hotelService.updateHotel(
                        1L,
                        request,
                        "other@test.com",
                        "MANAGER"
                )
        ).isInstanceOf(UnauthorizedException.class);
    }
    @Test
    void getHotelById_shouldReturnHotel() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Hotel A")
                .city("Delhi")
                .amenities("WIFI,POOL")
                .roomCategories(List.of())
                .build();

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        HotelResponse response = hotelService.getHotelById(1L);

        assertThat(response.getName()).isEqualTo("Hotel A");
        assertThat(response.getAmenities()).contains("POOL");
    }

    @Test
    void getHotelById_shouldThrow_whenNotFound() {

        when(hotelRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                hotelService.getHotelById(99L)
        ).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getHotelByManagerEmail_shouldReturnHotel() {

        Hotel hotel = Hotel.builder()
                .id(1L)
                .managerEmail("manager@test.com")
                .amenities("WIFI")
                .roomCategories(List.of())
                .build();

        when(hotelRepository.findFirstByManagerEmail("manager@test.com"))
                .thenReturn(Optional.of(hotel));

        HotelResponse response =
                hotelService.getHotelByManagerEmail("manager@test.com");

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getHotelByManagerEmail_shouldThrow_whenNotFound() {

        when(hotelRepository.findFirstByManagerEmail(any()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                hotelService.getHotelByManagerEmail("x@test.com")
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getHotelByReceptionistEmail_shouldReturnHotel() {

        HotelStaff staff = HotelStaff.builder()
                .hotelId(1L)
                .staffEmail("rec@test.com")
                .role(Role.RECEPTIONIST)
                .build();

        Hotel hotel = Hotel.builder()
                .id(1L)
                .amenities("WIFI")
                .roomCategories(List.of())
                .build();

        when(hotelStaffRepository
                .findByStaffEmailAndRole("rec@test.com", Role.RECEPTIONIST))
                .thenReturn(Optional.of(staff));

        when(hotelRepository.findById(1L))
                .thenReturn(Optional.of(hotel));

        HotelResponse response =
                hotelService.getHotelByReceptionistEmail("rec@test.com");

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getHotelByReceptionistEmail_shouldThrow_whenNotAssigned() {

        when(hotelStaffRepository
                .findByStaffEmailAndRole(any(), eq(Role.RECEPTIONIST)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                hotelService.getHotelByReceptionistEmail("rec@test.com")
        ).isInstanceOf(ResourceNotFoundException.class);
    }
}
