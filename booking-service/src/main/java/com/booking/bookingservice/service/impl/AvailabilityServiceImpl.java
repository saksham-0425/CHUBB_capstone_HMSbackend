package com.booking.bookingservice.service.impl;

import com.booking.bookingservice.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final RedisTemplate<String, Integer> redisTemplate;

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
            Integer available =
                redisTemplate.opsForValue()
                    .get(key(hotelId, categoryId, date));

            if (available == null || available <= 0) {
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
            redisTemplate.opsForValue()
                .increment(key(hotelId, categoryId, date));
            date = date.plusDays(1);
        }
    }
}
