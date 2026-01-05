package com.booking.bookingservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.booking.bookingservice.client.HotelServiceClient;
import com.booking.bookingservice.dto.response.CategoryAvailabilityResponse;
import com.booking.bookingservice.dto.response.HotelAvailabilityResponse;
import com.booking.bookingservice.dto.response.RoomCategoryResponseDto;
import com.booking.bookingservice.service.impl.AvailabilityServiceImpl;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HotelServiceClient hotelServiceClient;

    @Mock
    private ValueOperations<String, String> valueOperations;
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    void isAvailable_whenEnoughRooms_shouldReturnTrue() {

        when(valueOperations.get(anyString()))
                .thenReturn(null); 

        RoomCategoryResponseDto category = new RoomCategoryResponseDto();
        category.setId(2L);
        category.setTotalRooms(5);

        when(hotelServiceClient.getCategoryById(2L))
                .thenReturn(category);

        boolean result = availabilityService.isAvailable(
                1L,
                2L,
                LocalDate.now(),
                LocalDate.now().plusDays(2),
                2
        );

        assertTrue(result);
    }
    
    @Test
    void isAvailable_whenInsufficientRooms_shouldReturnFalse() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get(anyString())).thenReturn("1");

        boolean available = availabilityService.isAvailable(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        assertFalse(available);
    }
    
    @Test
    void reserve_shouldDecrementAvailabilityForEachDate() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get(anyString())).thenReturn("5");

        availabilityService.reserve(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        verify(valueOperations, atLeastOnce())
                .decrement(anyString(), eq(2L));
    }
    
    @Test
    void release_shouldIncrementAvailabilityForEachDate() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get(anyString())).thenReturn("3");

        availabilityService.release(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        verify(valueOperations, atLeastOnce())
                .increment(anyString(), eq(2L));
    }
    
    @Test
    void getHotelAvailability_shouldReturnMinAvailabilityPerCategory() {

        RoomCategoryResponseDto category = new RoomCategoryResponseDto();
        category.setId(1L);
        category.setCategory("DELUXE");
        category.setBasePrice(BigDecimal.valueOf(2000));

        when(hotelServiceClient.getCategoriesByHotel(1L))
                .thenReturn(List.of(category));

        when(valueOperations.get(anyString()))
                .thenReturn("5", "3"); // min = 3

        HotelAvailabilityResponse response =
                availabilityService.getHotelAvailability(
                        1L,
                        LocalDate.now(),
                        LocalDate.now().plusDays(2)
                );

        assertEquals(1, response.getCategories().size());
        assertEquals(3, response.getCategories().get(0).getAvailableRooms());
    }

    

}
