package com.hotel.hotelservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.hotelservice.dto.request.BulkCreateRoomRequest;
import com.hotel.hotelservice.dto.request.CreateRoomRequest;
import com.hotel.hotelservice.dto.request.RoomStatusUpdateRequest;
import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;
import com.hotel.hotelservice.service.RoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{hotelId}/rooms/suggest")
    public ResponseEntity<RoomSuggestionResponse> suggestRoom(
            @PathVariable Long hotelId,
            @RequestParam Long categoryId,
            @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(
                roomService.suggestRoom(hotelId, categoryId, role)
        );
    }
    
    @PutMapping("/rooms/{roomId}/status")
    public ResponseEntity<Void> updateRoomStatus(
            @PathVariable Long roomId,
            @RequestBody RoomStatusUpdateRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        roomService.updateRoomStatus(
                roomId,
                request.getStatus(),
                role
        );
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{hotelId}/rooms")
    public ResponseEntity<Void> createRoom(
            @PathVariable Long hotelId,
            @RequestBody CreateRoomRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        roomService.createRoom(hotelId, request, role);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/{hotelId}/rooms/bulk")
    public ResponseEntity<Void> bulkCreateRooms(
            @PathVariable Long hotelId,
            @RequestBody BulkCreateRoomRequest request,
            @RequestHeader("X-User-Role") String role
    ) {
        roomService.bulkCreateRooms(hotelId, request, role);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
