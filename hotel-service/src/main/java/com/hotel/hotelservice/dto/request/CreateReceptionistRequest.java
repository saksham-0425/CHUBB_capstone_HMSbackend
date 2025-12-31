package com.hotel.hotelservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReceptionistRequest {

    @Email
    @NotBlank
    private String receptionistEmail;

}
