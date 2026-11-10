
package com.nexora.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record RescheduleAppointmentRequest(

        @NotNull(message = "La nueva fecha y hora son obligatorias")
        OffsetDateTime newStartAt

) {}
