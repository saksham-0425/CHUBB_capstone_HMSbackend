package com.hotel.hotelservice.repository;

import com.hotel.hotelservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
// Used for PUBLIC hotel search by city
    List<Hotel> findByCityIgnoreCase(String city);

//   Used to verify manager ownership 
    Optional<Hotel> findByIdAndManagerEmail(Long id, String managerEmail);
    
// Optional: used if manager dashboard is added later 
    List<Hotel> findByManagerEmail(String managerEmail);
}
