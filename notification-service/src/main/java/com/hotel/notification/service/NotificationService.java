package com.hotel.notification.service;

import com.hotel.notification.dto.BookingEventDTO;

public interface NotificationService {

    void processEvent(BookingEventDTO event);
}
