package com.hotel.hotelservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.hotelservice.dto.request.RoomCategoryRequest;
import com.hotel.hotelservice.dto.response.RoomCategoryResponse;
import com.hotel.hotelservice.service.RoomCategoryService;

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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoomCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class RoomCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomCategoryService roomCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addCategory_shouldReturn201_forAdmin() throws Exception {

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setCategory("DELUXE");
        request.setTotalRooms(10);
        request.setCapacity(2);
        request.setBasePrice(3000.0);

        RoomCategoryResponse response = RoomCategoryResponse.builder()
                .id(1L)
                .category("DELUXE")
                .totalRooms(10)
                .capacity(2)
                .basePrice(3000.0)
                .build();

        when(roomCategoryService.addCategory(eq(1L), any(), eq("ADMIN")))
                .thenReturn(response);

        mockMvc.perform(post("/hotels/1/categories")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("DELUXE"))
                .andExpect(jsonPath("$.totalRooms").value(10));
    }

    @Test
    void updateCategory_shouldReturn200_forManager() throws Exception {

        RoomCategoryRequest request = new RoomCategoryRequest();
        request.setCategory("DELUXE");
        request.setTotalRooms(15);
        request.setCapacity(3);
        request.setBasePrice(3500.0);

        RoomCategoryResponse response = RoomCategoryResponse.builder()
                .id(5L)
                .category("DELUXE")
                .totalRooms(15)
                .capacity(3)
                .basePrice(3500.0)
                .build();

        when(roomCategoryService.updateCategory(
                eq(5L),
                any(),
                eq("manager@test.com"),
                eq("MANAGER")
        )).thenReturn(response);

        mockMvc.perform(put("/hotels/categories/5")
                        .header("X-User-Email", "manager@test.com")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").value(15))
                .andExpect(jsonPath("$.basePrice").value(3500.0));
    }

    @Test
    void getCategoriesByHotel_shouldReturn200() throws Exception {

        when(roomCategoryService.getCategoriesByHotel(1L))
                .thenReturn(List.of(
                        RoomCategoryResponse.builder()
                                .id(1L)
                                .category("STANDARD")
                                .build(),
                        RoomCategoryResponse.builder()
                                .id(2L)
                                .category("DELUXE")
                                .build()
                ));

        mockMvc.perform(get("/hotels/1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("STANDARD"))
                .andExpect(jsonPath("$[1].category").value("DELUXE"));
    }

    @Test
    void getCategoryById_shouldReturn200() throws Exception {

        when(roomCategoryService.getCategoryById(3L))
                .thenReturn(RoomCategoryResponse.builder()
                        .id(3L)
                        .category("SUITE")
                        .capacity(4)
                        .basePrice(6000.0)
                        .build());

        mockMvc.perform(get("/internal/hotels/categories/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("SUITE"))
                .andExpect(jsonPath("$.capacity").value(4));
    }

    @Test
    void addCategory_shouldReturn400_whenInvalidRequest() throws Exception {

        RoomCategoryRequest invalidRequest = new RoomCategoryRequest();
        invalidRequest.setCategory("");     
        invalidRequest.setTotalRooms(0);  
        invalidRequest.setCapacity(0);     
        invalidRequest.setBasePrice(0.0);   

        mockMvc.perform(post("/hotels/1/categories")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
