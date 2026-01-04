package com.hotel.hotelservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.hotelservice.entity.HotelStaff;
import com.hotel.hotelservice.entity.Role;

public interface HotelStaffRepository
extends JpaRepository<HotelStaff, Long> {

boolean existsByHotelIdAndStaffEmail(Long hotelId, String email);

Optional<HotelStaff> findByStaffEmail(String email);
Optional<HotelStaff> findByStaffEmailAndRole(String staffEmail, Role role);
}
