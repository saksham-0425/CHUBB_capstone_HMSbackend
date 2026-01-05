package com.hotel.hotelservice.repository;

import com.hotel.hotelservice.entity.Hotel;
import com.hotel.hotelservice.entity.RoomCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomCategoryRepositoryTest {

    @Autowired
    private RoomCategoryRepository roomCategoryRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Hotel saveHotel() {
        return hotelRepository.save(
                Hotel.builder()
                        .name("Hotel A")
                        .city("Delhi")
                        .address("Address")
                        .managerEmail("manager@test.com")
                        .amenities("WIFI")
                        .roomCategories(List.of())
                        .build()
        );
    }

    private RoomCategory saveCategory(
            Hotel hotel,
            String category
    ) {
        return roomCategoryRepository.save(
                RoomCategory.builder()
                        .category(category)
                        .totalRooms(10)
                        .capacity(2)
                        .basePrice(2000.0)
                        .hotel(hotel)
                        .build()
        );
    }

    @Test
    void findByHotelId_shouldReturnAllCategoriesForHotel() {

        Hotel hotel1 = saveHotel();
        Hotel hotel2 = saveHotel();

        saveCategory(hotel1, "DELUXE");
        saveCategory(hotel1, "STANDARD");
        saveCategory(hotel2, "SUITE");

        List<RoomCategory> categories =
                roomCategoryRepository.findByHotelId(hotel1.getId());

        assertThat(categories).hasSize(2);
        assertThat(categories)
                .extracting(RoomCategory::getCategory)
                .containsExactlyInAnyOrder("DELUXE", "STANDARD");
    }

    @Test
    void findByIdAndHotelId_shouldReturnCategory_whenMatches() {

        Hotel hotel = saveHotel();
        RoomCategory category = saveCategory(hotel, "DELUXE");

        Optional<RoomCategory> result =
                roomCategoryRepository.findByIdAndHotelId(
                        category.getId(),
                        hotel.getId()
                );

        assertThat(result).isPresent();
        assertThat(result.get().getCategory()).isEqualTo("DELUXE");
    }

    @Test
    void findByIdAndHotelId_shouldReturnEmpty_whenHotelMismatch() {

        Hotel hotel1 = saveHotel();
        Hotel hotel2 = saveHotel();

        RoomCategory category = saveCategory(hotel1, "DELUXE");

        Optional<RoomCategory> result =
                roomCategoryRepository.findByIdAndHotelId(
                        category.getId(),
                        hotel2.getId()
                );

        assertThat(result).isEmpty();
    }

    @Test
    void existsByHotelIdAndCategoryIgnoreCase_shouldReturnTrue_whenExists() {

        Hotel hotel = saveHotel();
        saveCategory(hotel, "DELUXE");

        boolean exists =
                roomCategoryRepository
                        .existsByHotelIdAndCategoryIgnoreCase(
                                hotel.getId(),
                                "deluxe"
                        );

        assertThat(exists).isTrue();
    }

    @Test
    void existsByHotelIdAndCategoryIgnoreCase_shouldReturnFalse_whenNotExists() {

        Hotel hotel = saveHotel();

        boolean exists =
                roomCategoryRepository
                        .existsByHotelIdAndCategoryIgnoreCase(
                                hotel.getId(),
                                "suite"
                        );

        assertThat(exists).isFalse();
    }
}
