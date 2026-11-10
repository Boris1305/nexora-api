package com.nexora.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitPaymentReceiptRequest(

        @NotBlank(message = "La referencia del comprobante es obligatoria")
        @Size(max = 2000, message = "La referencia no puede superar los 2000 caracteres")
        String receiptReference

) {
}