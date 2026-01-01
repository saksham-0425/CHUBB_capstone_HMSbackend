package com.hotel.hotelservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    @Column(length = 1000)
    private String description;

    
//    Email of the manager assigned to this hotel
//    Comes from auth-service
    
    @Column(nullable = false)
    private String managerEmail;

    
//     Amenities stored as comma-separated values
//     Example: WIFI,POOL,PARKING
    
    @Column(length = 1000)
    private String amenities;

    
//   One hotel can have multiple room categories
     
    @OneToMany(
            mappedBy = "hotel",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RoomCategory> roomCategories;
}
