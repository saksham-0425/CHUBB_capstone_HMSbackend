package com.hotel.hotelservice.controller;

import com.hotel.hotelservice.dto.request.CreateHotelRequest;
import com.hotel.hotelservice.dto.request.CreateReceptionistRequest;
import com.hotel.hotelservice.dto.request.UpdateHotelRequest;
import com.hotel.hotelservice.dto.response.HotelResponse;
import com.hotel.hotelservice.service.HotelService;
import com.hotel.hotelservice.service.HotelStaffService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;
    private final HotelStaffService hotelStaffService;


//      ADMIN -> Create Hotel
    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateHotelRequest request
    ) {
        HotelResponse response = hotelService.createHotel(request, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

   
//    ADMIN / MANAGER -> Update Hotel
    
    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Long hotelId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Role") String role,
            @RequestBody UpdateHotelRequest request
    ) {
        HotelResponse response =
                hotelService.updateHotel(hotelId, request, userEmail, role);
        return ResponseEntity.ok(response);
    }

//   PUBLIC -> Get all hotels
    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }


//  PUBLIC -> Search hotels by city
    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotelsByCity(
            @RequestParam String city
    ) {
        return ResponseEntity.ok(hotelService.searchHotelsByCity(city));
    }


//      PUBLIC -> Get hotel by ID
    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotelById(
            @PathVariable Long hotelId
    ) {
        return ResponseEntity.ok(hotelService.getHotelById(hotelId));
    }
    
    // MANAGER -> add receptionist
    @PostMapping("/{hotelId}/receptionists")
    public ResponseEntity<Void> addReceptionist(
            @PathVariable Long hotelId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String managerEmail,
            @Valid @RequestBody CreateReceptionistRequest request
    ) {
        hotelStaffService.addReceptionist(
            hotelId,
            managerEmail,
            role,
            request
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("internal/manager")
    public HotelResponse getHotelByManager(
            @RequestHeader("X-User-Email") String managerEmail,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"MANAGER".equals(role)) {
            throw new SecurityException("Only manager allowed");
        }

        return hotelService.getHotelByManagerEmail(managerEmail);
    }
   
    @GetMapping("/internal/receptionist")
    public HotelResponse getHotelByReceptionist(
            @RequestHeader("X-User-Email") String receptionistEmail,
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"RECEPTIONIST".equals(role)) {
            throw new SecurityException("Only receptionist allowed");
        }

        return hotelService.getHotelByReceptionistEmail(receptionistEmail);
    }
}
