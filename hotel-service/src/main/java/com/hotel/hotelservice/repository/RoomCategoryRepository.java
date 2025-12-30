package com.hotel.hotelservice.repository;

import com.hotel.hotelservice.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {


//     Get all categories for a hotel
//     Used by PUBLIC APIs and booking-service
    List<RoomCategory> findByHotelId(Long hotelId);
    
//   Used for RBAC checks while updating category
    Optional<RoomCategory> findByIdAndHotelId(Long id, Long hotelId);

//  Prevent duplicate category creation per hotel
    boolean existsByHotelIdAndCategoryIgnoreCase(Long hotelId, String category);
}
