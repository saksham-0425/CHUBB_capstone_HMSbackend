package com.hotel.hotelservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "room_categories",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hotel_id", "category"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
//     STANDARD, DELUXE, SUITE, etc.
     
    @Column(nullable = false)
    private String category;


//      Total number of rooms for this category
//      Used by booking-service for Redis availability

    @Column(nullable = false)
    private Integer totalRooms;

    
//    Max number of guests allowed per room
     
    @Column(nullable = false)
    private Integer capacity;

    
//    Base price per night
     
    @Column(nullable = false)
    private Double basePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}
