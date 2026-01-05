package com.hotel.auth.dto;


import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {}
