package com.booking.bookingservice.util;

import java.util.UUID;

public final class BookingReferenceGenerator {

    private BookingReferenceGenerator() {
        // prevent instantiation
    }

    public static String generate() {
        return "BK-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}
