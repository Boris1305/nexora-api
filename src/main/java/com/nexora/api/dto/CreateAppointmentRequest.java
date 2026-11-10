package com.nexora.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateAppointmentRequest(

        @NotNull(message = "El ID del cliente es obligatorio")
        Long clientId,

        @NotNull(message = "El ID del servicio es obligatorio")
        Long serviceId,

        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        OffsetDateTime startAt

) {
}