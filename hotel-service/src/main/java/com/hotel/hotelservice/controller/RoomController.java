package com.hotel.hotelservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
