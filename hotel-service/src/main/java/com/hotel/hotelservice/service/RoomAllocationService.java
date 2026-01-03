package com.hotel.hotelservice.service;

public interface RoomAllocationService {

//Allocate a room for a booking (called on booking check-in).
// Auto-suggests the best available room.
  
    void allocateRoom(Long bookingId, Long hotelId, Long categoryId);

//Release the allocated room for a booking (called on booking check-out).
   
    void releaseRoom(Long bookingId);
}
