package com.hotel.hotelservice.repository;

import com.hotel.hotelservice.entity.Room;
import com.hotel.hotelservice.entity.RoomStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    private Room saveRoom(
            Long hotelId,
            Long categoryId,
            String roomNumber,
            RoomStatus status
    ) {
        return roomRepository.save(
                Room.builder()
                        .hotelId(hotelId)
                        .categoryId(categoryId)
                        .roomNumber(roomNumber)
                        .status(status)
                        .build()
        );
    }

    @Test
    void findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc_shouldReturnSortedRooms() {

        saveRoom(1L, 10L, "103", RoomStatus.AVAILABLE);
        saveRoom(1L, 10L, "101", RoomStatus.AVAILABLE);
        saveRoom(1L, 10L, "102", RoomStatus.AVAILABLE);
        saveRoom(1L, 10L, "104", RoomStatus.OCCUPIED); // ignored
        saveRoom(2L, 10L, "105", RoomStatus.AVAILABLE); // ignored

        List<Room> rooms =
                roomRepository
                        .findByHotelIdAndCategoryIdAndStatusOrderByRoomNumberAsc(
                                1L,
                                10L,
                                RoomStatus.AVAILABLE
                        );

        assertThat(rooms).hasSize(3);
        assertThat(rooms)
                .extracting(Room::getRoomNumber)
                .containsExactly("101", "102", "103");
    }

    @Test
    void existsByHotelIdAndRoomNumber_shouldReturnTrue_whenExists() {

        saveRoom(1L, 10L, "201", RoomStatus.AVAILABLE);

        boolean exists =
                roomRepository.existsByHotelIdAndRoomNumber(1L, "201");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByHotelIdAndRoomNumber_shouldReturnFalse_whenNotExists() {

        boolean exists =
                roomRepository.existsByHotelIdAndRoomNumber(1L, "999");

        assertThat(exists).isFalse();
    }
    @Test
    void findByHotelId_shouldReturnAllRoomsForHotel() {

        saveRoom(1L, 10L, "301", RoomStatus.AVAILABLE);
        saveRoom(1L, 11L, "302", RoomStatus.OCCUPIED);
        saveRoom(2L, 10L, "303", RoomStatus.AVAILABLE);

        List<Room> rooms =
                roomRepository.findByHotelId(1L);

        assertThat(rooms).hasSize(2);
        assertThat(rooms)
                .extracting(Room::getRoomNumber)
                .containsExactlyInAnyOrder("301", "302");
    }

    @Test
    void findByHotelId_shouldReturnEmpty_whenNoRooms() {

        List<Room> rooms =
                roomRepository.findByHotelId(99L);

        assertThat(rooms).isEmpty();
    }
}
