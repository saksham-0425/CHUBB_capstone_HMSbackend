package com.hotel.hotelservice.controller;

import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.response.RoomCategoryResponse;
import com.hotel.hotelservice.service.RoomCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomCategoryController {

    private final RoomCategoryService roomCategoryService;

  
//     ADMIN → Add room category to hotel
    @PostMapping("/hotels/{hotelId}/categories")
    public ResponseEntity<RoomCategoryResponse> addCategory(
            @PathVariable Long hotelId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody RoomCategoryRequest request
    ) {
        RoomCategoryResponse response =
                roomCategoryService.addCategory(hotelId, request, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
//   ADMIN / MANAGER → Update category inventory or pricing
    @PutMapping("/hotels/categories/{categoryId}")
    public ResponseEntity<RoomCategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody RoomCategoryRequest request
    ) {
        RoomCategoryResponse response =
                roomCategoryService.updateCategory(categoryId, request, userEmail, role);
        return ResponseEntity.ok(response);
    }

   
//  PUBLIC → Get categories of a hotel
    @GetMapping("/hotels/{hotelId}/categories")
    public ResponseEntity<List<RoomCategoryResponse>> getCategoriesByHotel(
            @PathVariable Long hotelId
    ) {
        return ResponseEntity.ok(
                roomCategoryService.getCategoriesByHotel(hotelId)
        );
    }
}
