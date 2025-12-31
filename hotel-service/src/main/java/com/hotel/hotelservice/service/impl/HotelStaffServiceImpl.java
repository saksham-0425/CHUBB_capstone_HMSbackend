package com.hotel.hotelservice.service.impl;

import com.hotel.hotelservice.dto.request.CreateReceptionistRequest;
import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.HotelStaff;
import com.hotel.hotelservice.exception.ResourceNotFoundException;
import com.hotel.hotelservice.exception.UnauthorizedException;
import com.hotel.hotelservice.entity.HotelStaff;
import com.hotel.hotelservice.repository.HotelRepository;
import com.hotel.hotelservice.repository.HotelStaffRepository;
import com.hotel.hotelservice.service.HotelStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional
public class HotelStaffServiceImpl implements HotelStaffService {

    private final HotelRepository hotelRepository;
    private final HotelStaffRepository hotelStaffRepository;

    @Override
    public void addReceptionist(
            Long hotelId,
            String managerEmail,
            String role,
            CreateReceptionistRequest request
    ) {

        if (!"MANAGER".equals(role)) {
            throw new UnauthorizedException("Only MANAGER can add receptionist");
        }

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hotel not found")
                );

        if (!hotel.getManagerEmail().equals(managerEmail)) {
            throw new UnauthorizedException("You are not manager of this hotel");
        }

        String receptionistEmail = request.getReceptionistEmail();

        if (hotelStaffRepository.existsByHotelIdAndStaffEmail(
                hotelId, receptionistEmail)) {
            throw new IllegalArgumentException(
                    "Receptionist already assigned to this hotel"
            );
        }

        HotelStaff staff = HotelStaff.builder()
                .hotelId(hotelId)
                .staffEmail(receptionistEmail)
                .role(com.hotel.hotelservice.entity.Role.RECEPTIONIST)
                .build();

        hotelStaffRepository.save(staff);
    }
}
