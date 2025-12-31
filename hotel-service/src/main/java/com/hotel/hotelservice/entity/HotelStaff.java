package com.hotel.hotelservice.entity;


import com.hotel.hotelservice.entity.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelStaff {

    @Id
    @GeneratedValue
    private Long id;

    private Long hotelId;

    private String staffEmail;

    @Enumerated(EnumType.STRING)
    private Role role;
}
