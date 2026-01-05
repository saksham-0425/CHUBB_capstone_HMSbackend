package com.hotel.hotelservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.hotelservice.dto.request.AllocateRoomRequest;
import com.hotel.hotelservice.dto.request.ReleaseRoomRequest;
import com.hotel.hotelservice.service.RoomAllocationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoomAllocationInternalController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RoomAllocationInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomAllocationService roomAllocationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void allocateRoom_shouldReturn200() throws Exception {

        AllocateRoomRequest request = new AllocateRoomRequest();
        request.setBookingId(1L);
        request.setHotelId(10L);
        request.setCategoryId(5L);
        request.setNumberOfRooms(2);

        doNothing().when(roomAllocationService)
                .allocateRooms(
                        eq(1L),
                        eq(10L),
                        eq(5L),
                        eq(2)
                );

        mockMvc.perform(post("/internal/room-allocations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    @Test
    void releaseRoom_shouldReturn200() throws Exception {

        ReleaseRoomRequest request = new ReleaseRoomRequest();
        request.setBookingId(1L);

        doNothing().when(roomAllocationService)
                .releaseRooms(1L);

        mockMvc.perform(post("/internal/room-allocations/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
