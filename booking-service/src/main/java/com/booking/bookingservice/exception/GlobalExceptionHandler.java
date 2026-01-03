package com.booking.bookingservice.exception;

import com.booking.bookingservice.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔴 400 – Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return build(HttpStatus.BAD_REQUEST, message);
    }

    // 🔴 400 – Room unavailable
    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRoomUnavailable(
            RoomNotAvailableException ex
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 🔴 400 – Invalid state transitions
    @ExceptionHandler(InvalidReservationStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(
            InvalidReservationStateException ex
    ) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 🔴 401 – Unauthorized actions
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex
    ) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // 🔴 404 – Booking not found
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ReservationNotFoundException ex
    ) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 🔴 500 – Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex
    ) {
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong"
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
    
    @ExceptionHandler(InvalidGuestCountException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidGuestCount(
            InvalidGuestCountException ex) {

        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }


}
