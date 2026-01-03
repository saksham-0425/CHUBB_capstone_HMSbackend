package com.hotel.hotelservice.service;

public interface RoomAllocationService {

//Allocate a room for a booking (called on booking check-in).
// Auto-suggests the best available room.
  
    void allocateRooms(Long bookingId, Long hotelId, Long categoryId, int numberOfRooms);

//Release the allocated room for a booking (called on booking check-out).
   
    void releaseRooms(Long bookingId);
    
    
}
