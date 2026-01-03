package com.hotel.hotelservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotel.hotelservice.dto.request.AllocateRoomRequest;
import com.hotel.hotelservice.dto.request.ReleaseRoomRequest;
import com.hotel.hotelservice.service.RoomAllocationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/room-allocations")
@RequiredArgsConstructor
public class RoomAllocationInternalController {

    private final RoomAllocationService roomAllocationService;

 
    @PostMapping
    public ResponseEntity<Void> allocateRoom(
            @RequestBody AllocateRoomRequest request
    ) {
        roomAllocationService.allocateRooms(
                request.getBookingId(),
                request.getHotelId(),
                request.getCategoryId(),
                request.getNumberOfRooms()
        );
        return ResponseEntity.ok().build();
    }


    @PostMapping("/release")
    public ResponseEntity<Void> releaseRoom(
            @RequestBody ReleaseRoomRequest request
    ) {
        roomAllocationService.releaseRooms(request.getBookingId());
        return ResponseEntity.ok().build();
    }
}
