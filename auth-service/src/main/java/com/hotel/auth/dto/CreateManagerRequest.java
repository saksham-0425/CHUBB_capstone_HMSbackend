package com.hotel.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CreateManagerRequest {

    @Email
    private String email;

    @NotBlank
    private String password;

   
}
