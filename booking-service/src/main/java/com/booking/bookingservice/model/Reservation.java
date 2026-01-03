package com.booking.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Public reference 
    @Column(nullable = false, unique = true)
    private String bookingReference;

    // Comes from JWT (gateway)
    @Column(nullable = false)
    private String userEmail;
    
   
    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Column(nullable = false)
    private Integer numberOfRooms;

    // Foreign references 
    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long roomCategoryId;

    // Booking dates
    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    // Pricing snapshot
    @Column(nullable = false)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;
    @Column(nullable = false)
    private Boolean checkInReminderSent = false;
    @Column(nullable = false)
    private Boolean checkOutReminderSent = false;

}
