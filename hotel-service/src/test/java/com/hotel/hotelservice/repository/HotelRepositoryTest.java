package com.hotel.hotelservice.repository;

import com.hotel.hotelservice.entity.Hotel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel saveHotel(
            String name,
            String city,
            String managerEmail
    ) {
        return hotelRepository.save(
                Hotel.builder()
                        .name(name)
                        .city(city)
                        .address("Some Address")
                        .managerEmail(managerEmail)
                        .amenities("WIFI,POOL")
                        .roomCategories(List.of())
                        .build()
        );
    }

    @Test
    void findByCityIgnoreCase_shouldReturnHotelsIgnoringCase() {

        saveHotel("Hotel A", "Mumbai", "a@test.com");
        saveHotel("Hotel B", "mumbai", "b@test.com");
        saveHotel("Hotel C", "Delhi", "c@test.com");

        List<Hotel> hotels =
                hotelRepository.findByCityIgnoreCase("MUMBAI");

        assertThat(hotels).hasSize(2);
        assertThat(hotels)
                .extracting(Hotel::getName)
                .containsExactlyInAnyOrder("Hotel A", "Hotel B");
    }

    @Test
    void findByIdAndManagerEmail_shouldReturnHotel_whenMatches() {

        Hotel hotel =
                saveHotel("Hotel A", "Delhi", "manager@test.com");

        Optional<Hotel> result =
                hotelRepository.findByIdAndManagerEmail(
                        hotel.getId(),
                        "manager@test.com"
                );

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Hotel A");
    }

    @Test
    void findByIdAndManagerEmail_shouldReturnEmpty_whenEmailMismatch() {

        Hotel hotel =
                saveHotel("Hotel A", "Delhi", "manager@test.com");

        Optional<Hotel> result =
                hotelRepository.findByIdAndManagerEmail(
                        hotel.getId(),
                        "other@test.com"
                );

        assertThat(result).isEmpty();
    }

    @Test
    void findByManagerEmail_shouldReturnAllHotelsForManager() {

        saveHotel("Hotel A", "Delhi", "manager@test.com");
        saveHotel("Hotel B", "Mumbai", "manager@test.com");
        saveHotel("Hotel C", "Delhi", "other@test.com");

        List<Hotel> hotels =
                hotelRepository.findByManagerEmail("manager@test.com");

        assertThat(hotels).hasSize(2);
    }

    @Test
    void findFirstByManagerEmail_shouldReturnOneHotel() {

        saveHotel("Hotel A", "Delhi", "manager@test.com");
        saveHotel("Hotel B", "Mumbai", "manager@test.com");

        Optional<Hotel> hotel =
                hotelRepository.findFirstByManagerEmail("manager@test.com");

        assertThat(hotel).isPresent();
    }

    @Test
    void findFirstByManagerEmail_shouldReturnEmpty_whenNoneExist() {

        Optional<Hotel> hotel =
                hotelRepository.findFirstByManagerEmail("missing@test.com");

        assertThat(hotel).isEmpty();
    }
}
