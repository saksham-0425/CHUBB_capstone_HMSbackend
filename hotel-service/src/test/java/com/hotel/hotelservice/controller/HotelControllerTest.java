package com.hotel.hotelservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.hotelservice.dto.request.CreateHotelRequest;
import com.hotel.hotelservice.dto.request.CreateReceptionistRequest;
import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.request.UpdateHotelRequest;
import com.hotel.hotelservice.dto.response.HotelResponse;
import com.hotel.hotelservice.service.HotelService;
import com.hotel.hotelservice.service.HotelStaffService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = HotelController.class)
@AutoConfigureMockMvc(addFilters = false)
class HotelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private HotelStaffService hotelStaffService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createHotel_shouldReturn201() throws Exception {

        RoomCategoryRequest category = new RoomCategoryRequest();
        category.setCategory("DELUXE");
        category.setTotalRooms(10);
        category.setCapacity(2);
        category.setBasePrice(3000.0);

        CreateHotelRequest request = CreateHotelRequest.builder()
                .name("Taj")
                .city("Mumbai")
                .address("Marine Drive")
                .description("Luxury hotel")
                .managerEmail("manager@test.com")
                .amenities(List.of("WIFI", "POOL"))
                .roomCategories(List.of(category))
                .build();

        HotelResponse response = HotelResponse.builder()
                .id(1L)
                .name("Taj")
                .city("Mumbai")
                .address("Marine Drive")
                .managerEmail("manager@test.com")
                .amenities(List.of("WIFI", "POOL"))
                .build();

        when(hotelService.createHotel(any(), eq("ADMIN")))
                .thenReturn(response);

        mockMvc.perform(post("/hotels")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Taj"));
    }


    @Test
    void updateHotel_shouldReturn200() throws Exception {

        UpdateHotelRequest request = UpdateHotelRequest.builder()
                .name("Updated Hotel")
                .build();

        HotelResponse response = HotelResponse.builder()
                .id(1L)
                .name("Updated Hotel")
                .build();

        when(hotelService.updateHotel(eq(1L), any(), eq("manager@test.com"), eq("MANAGER")))
                .thenReturn(response);

        mockMvc.perform(put("/hotels/1")
                        .header("X-User-Email", "manager@test.com")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Hotel"));
    }

    @Test
    void getAllHotels_shouldReturn200() throws Exception {

        when(hotelService.getAllHotels())
                .thenReturn(List.of(
                        HotelResponse.builder().id(1L).name("Hotel A").build(),
                        HotelResponse.builder().id(2L).name("Hotel B").build()
                ));

        mockMvc.perform(get("/hotels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchHotelsByCity_shouldReturn200() throws Exception {

        when(hotelService.searchHotelsByCity("Delhi"))
                .thenReturn(List.of(
                        HotelResponse.builder().id(1L).city("Delhi").build()
                ));

        mockMvc.perform(get("/hotels/search")
                        .param("city", "Delhi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Delhi"));
    }

    @Test
    void getHotelById_shouldReturn200() throws Exception {

        when(hotelService.getHotelById(1L))
                .thenReturn(HotelResponse.builder().id(1L).name("Hotel A").build());

        mockMvc.perform(get("/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void addReceptionist_shouldReturn201() throws Exception {

        CreateReceptionistRequest request =
                CreateReceptionistRequest.builder()
                        .receptionistEmail("receptionist@test.com")
                        .build();

        doNothing().when(hotelStaffService)
                .addReceptionist(eq(1L), eq("manager@test.com"), eq("MANAGER"), any());

        mockMvc.perform(post("/hotels/1/receptionists")
                        .header("X-User-Role", "MANAGER")
                        .header("X-User-Email", "manager@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getHotelByManager_shouldReturn200() throws Exception {

        when(hotelService.getHotelByManagerEmail("manager@test.com"))
                .thenReturn(HotelResponse.builder().id(1L).build());

        mockMvc.perform(get("/hotels/internal/manager")
                        .header("X-User-Email", "manager@test.com")
                        .header("X-User-Role", "MANAGER"))
                .andExpect(status().isOk());
    }

    @Test
    void getHotelByManager_shouldFailForNonManager() throws Exception {

        mockMvc.perform(get("/hotels/internal/manager")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getHotelByReceptionist_shouldReturn200() throws Exception {

        when(hotelService.getHotelByReceptionistEmail("receptionist@test.com"))
                .thenReturn(HotelResponse.builder().id(1L).build());

        mockMvc.perform(get("/hotels/internal/receptionist")
                        .header("X-User-Email", "receptionist@test.com")
                        .header("X-User-Role", "RECEPTIONIST"))
                .andExpect(status().isOk());
    }
}
