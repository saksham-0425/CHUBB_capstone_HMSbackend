package com.booking.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stay_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StayRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One stay per reservation
    @OneToOne(optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

}
