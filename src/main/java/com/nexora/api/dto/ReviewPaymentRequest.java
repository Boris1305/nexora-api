package com.nexora.api.dto;

import jakarta.validation.constraints.Size;

public record ReviewPaymentRequest(

        @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres")
        String reviewNotes

) {
}