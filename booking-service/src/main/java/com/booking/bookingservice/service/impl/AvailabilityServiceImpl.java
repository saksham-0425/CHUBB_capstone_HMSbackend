package com.booking.bookingservice.service.impl;

import com.booking.bookingservice.client.HotelServiceClient;
import com.booking.bookingservice.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final StringRedisTemplate redisTemplate;
    private final HotelServiceClient hotelServiceClient;

    private String key(Long hotelId, Long categoryId, LocalDate date) {
        return String.format(
                "availability:%d:%d:%s",
                hotelId,
                categoryId,
                date
        );
    }

    @Override
    public boolean isAvailable(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {

        LocalDate date = checkIn;

        while (date.isBefore(checkOut)) {

            Integer available = initializeIfMissing(
                    hotelId, categoryId, date
            );

            if (available <= 0) {
                return false;
            }

            date = date.plusDays(1);
        }

        return true;
    }

    @Override
    public void reserve(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {

        LocalDate date = checkIn;

        while (date.isBefore(checkOut)) {

            initializeIfMissing(hotelId, categoryId, date);

            redisTemplate.opsForValue()
                    .decrement(key(hotelId, categoryId, date));

            date = date.plusDays(1);
        }
    }

    @Override
    public void release(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {

        LocalDate date = checkIn;

        while (date.isBefore(checkOut)) {

            initializeIfMissing(hotelId, categoryId, date);

            redisTemplate.opsForValue()
                    .increment(key(hotelId, categoryId, date));

            date = date.plusDays(1);
        }
    }

    private Integer initializeIfMissing(
            Long hotelId,
            Long categoryId,
            LocalDate date
    ) {
        String redisKey = key(hotelId, categoryId, date);

        String value = redisTemplate.opsForValue().get(redisKey);

        if (value == null) {
            int totalRooms =
                    hotelServiceClient
                            .getCategoryById(categoryId)
                            .getTotalRooms();

            redisTemplate.opsForValue()
                    .set(redisKey, String.valueOf(totalRooms));

            return totalRooms;
        }

        return Integer.parseInt(value);
    }
}