package com.hotel.hotelservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
            Long hotelId,
            Long categoryId,
            RoomStatus status
    );
}
