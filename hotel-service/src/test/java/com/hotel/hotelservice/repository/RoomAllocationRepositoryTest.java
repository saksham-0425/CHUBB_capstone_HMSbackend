package com.hotel.hotelservice.repository;

import com.hotel.hotelservice.entity.RoomAllocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoomAllocationRepositoryTest {

    @Autowired
    private RoomAllocationRepository roomAllocationRepository;

    private RoomAllocation saveAllocation(
            Long bookingId,
            Long roomId,
            boolean released
    ) {
        return roomAllocationRepository.save(
                RoomAllocation.builder()
                        .bookingId(bookingId)
                        .roomId(roomId)
                        .allocatedAt(LocalDateTime.now())
                        .releasedAt(released ? LocalDateTime.now() : null)
                        .build()
        );
    }

    @Test
    void findByRoomIdAndReleasedAtIsNull_shouldReturnActiveAllocation() {

        saveAllocation(1L, 101L, false);
        saveAllocation(2L, 101L, true);

        Optional<RoomAllocation> result =
                roomAllocationRepository
                        .findByRoomIdAndReleasedAtIsNull(101L);

        assertThat(result).isPresent();
        assertThat(result.get().getBookingId()).isEqualTo(1L);
    }

    @Test
    void findByRoomIdAndReleasedAtIsNull_shouldReturnEmpty_whenNoneActive() {

        saveAllocation(1L, 101L, true);

        Optional<RoomAllocation> result =
                roomAllocationRepository
                        .findByRoomIdAndReleasedAtIsNull(101L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByBookingIdAndReleasedAtIsNull_shouldReturnActiveAllocation() {

        saveAllocation(10L, 201L, false);
        saveAllocation(10L, 202L, true);

        Optional<RoomAllocation> result =
                roomAllocationRepository
                        .findByBookingIdAndReleasedAtIsNull(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getRoomId()).isEqualTo(201L);
    }

    @Test
    void findByBookingIdAndReleasedAtIsNull_shouldReturnEmpty_whenNoneActive() {

        saveAllocation(10L, 201L, true);

        Optional<RoomAllocation> result =
                roomAllocationRepository
                        .findByBookingIdAndReleasedAtIsNull(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByBookingIdAndReleasedAtIsNull_shouldReturnAllActiveAllocations() {

        saveAllocation(99L, 301L, false);
        saveAllocation(99L, 302L, false);
        saveAllocation(99L, 303L, true);

        List<RoomAllocation> allocations =
                roomAllocationRepository
                        .findAllByBookingIdAndReleasedAtIsNull(99L);

        assertThat(allocations).hasSize(2);
        assertThat(allocations)
                .extracting(RoomAllocation::getRoomId)
                .containsExactlyInAnyOrder(301L, 302L);
    }

    @Test
    void findAllByBookingIdAndReleasedAtIsNull_shouldReturnEmpty_whenNoneActive() {

        saveAllocation(50L, 401L, true);

        List<RoomAllocation> allocations =
                roomAllocationRepository
                        .findAllByBookingIdAndReleasedAtIsNull(50L);

        assertThat(allocations).isEmpty();
    }
}
