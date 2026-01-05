package com.hotel.hotelservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.hotelservice.dto.request.BulkCreateRoomRequest;
import com.hotel.hotelservice.dto.request.CreateRoomRequest;
import com.hotel.hotelservice.dto.request.RoomStatusUpdateRequest;
import com.hotel.hotelservice.dto.response.RoomResponse;
import com.hotel.hotelservice.dto.response.RoomSuggestionResponse;
import com.hotel.hotelservice.service.RoomService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void suggestRoom_shouldReturn200() throws Exception {

        RoomResponse suggested = RoomResponse.builder()
                .roomId(1L)
                .roomNumber("101")
                .category("DELUXE")
                .status("AVAILABLE")
                .build();

        RoomSuggestionResponse response = RoomSuggestionResponse.builder()
                .suggestedRoom(suggested)
                .availableRooms(List.of(suggested))
                .build();

        when(roomService.suggestRoom(1L, 2L, "RECEPTIONIST"))
                .thenReturn(response);

        mockMvc.perform(get("/hotels/1/rooms/suggest")
                        .param("categoryId", "2")
                        .header("X-User-Role", "RECEPTIONIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedRoom.roomNumber").value("101"))
                .andExpect(jsonPath("$.availableRooms.length()").value(1));
    }

    @Test
    void updateRoomStatus_shouldReturn200() throws Exception {

        RoomStatusUpdateRequest request = new RoomStatusUpdateRequest();
        request.setStatus("CLEANING");

        doNothing().when(roomService)
                .updateRoomStatus(5L, "CLEANING", "MANAGER");

        mockMvc.perform(put("/hotels/rooms/5/status")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createRoom_shouldReturn201() throws Exception {

        CreateRoomRequest request = new CreateRoomRequest();
        request.setCategoryId(2L);
        request.setRoomNumber("202");

        doNothing().when(roomService)
                .createRoom(1L, request, "ADMIN");

        mockMvc.perform(post("/hotels/1/rooms")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void bulkCreateRooms_shouldReturn201() throws Exception {

        BulkCreateRoomRequest request = new BulkCreateRoomRequest();
        request.setCategoryId(2L);
        request.setStart(301);
        request.setEnd(310);

        doNothing().when(roomService)
                .bulkCreateRooms(1L, request, "MANAGER");

        mockMvc.perform(post("/hotels/1/rooms/bulk")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getRoomsByHotel_shouldReturn200() throws Exception {

        when(roomService.getRoomsByHotel(1L, "MANAGER", "manager@test.com"))
                .thenReturn(List.of(
                        RoomResponse.builder()
                                .roomId(1L)
                                .roomNumber("101")
                                .category("DELUXE")
                                .status("AVAILABLE")
                                .build(),
                        RoomResponse.builder()
                                .roomId(2L)
                                .roomNumber("102")
                                .category("DELUXE")
                                .status("OCCUPIED")
                                .build()
                ));

        mockMvc.perform(get("/hotels/1/rooms")
                        .header("X-User-Role", "MANAGER")
                        .header("X-User-Email", "manager@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomNumber").value("101"))
                .andExpect(jsonPath("$[1].status").value("OCCUPIED"));
    }
}
