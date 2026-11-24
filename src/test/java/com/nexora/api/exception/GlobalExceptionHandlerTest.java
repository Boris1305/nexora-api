package com.nexora.api.exception;

import com.nexora.api.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleValidationException() {

        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "name", "El nombre es obligatorio"),
                new FieldError("request", "price", "El precio debe ser positivo")
        ));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(request.getRequestURI()).thenReturn("/api/services");

        ResponseEntity<ApiErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals(
                "El nombre es obligatorio; El precio debe ser positivo",
                response.getBody().getMessage()
        );
        assertEquals("/api/services", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleResponseStatusException() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/payments/999");

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pago no encontrado"
        );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleResponseStatusException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("Pago no encontrado", response.getBody().getMessage());
        assertEquals("/api/payments/999", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void shouldHandleConflictException() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRequestURI()).thenReturn("/api/appointments/1");

        ResponseStatusException exception = new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede modificar el turno"
        );

        ResponseEntity<ApiErrorResponse> response =
                handler.handleResponseStatusException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals(
                "No se puede modificar el turno",
                response.getBody().getMessage()
        );
        assertEquals("/api/appointments/1", response.getBody().getPath());
    }
}
