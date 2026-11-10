
package com.nexora.api.dto;

import com.nexora.api.entity.AppointmentEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AppointmentResponse(
        ClientDetails client,
        ServiceDetails service,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        Integer durationMinutes,
        BigDecimal price,
        OffsetDateTime createdAt,
        Long id,
        String status,
        OffsetDateTime cancelledAt
) {

    public static AppointmentResponse from(AppointmentEntity appointment) {

        ClientDetails clientDetails = new ClientDetails(
                appointment.getClient().getFirstName(),
                appointment.getClient().getLastName(),
                appointment.getClient().getEmail(),
                appointment.getClient().getPhone(),
                appointment.getClient().getNotes(),
                appointment.getClient().getActive(),
                appointment.getClient().getCreatedAt(),
                appointment.getClient().getId()
        );

        ServiceDetails serviceDetails = new ServiceDetails(
                appointment.getService().getName(),
                appointment.getService().getDescription(),
                appointment.getService().getPrice(),
                appointment.getService().getDurationMinutes(),
                appointment.getService().getActive(),
                appointment.getService().getCreatedAt(),
                appointment.getService().getId()
        );

        return new AppointmentResponse(
                clientDetails,
                serviceDetails,
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getDurationMinutes(),
                appointment.getPrice(),
                appointment.getCreatedAt(),
                appointment.getId(),
                appointment.getStatus().name(),
                appointment.getCancelledAt()
        );
    }

    public record ClientDetails(
            String firstName,
            String lastName,
            String email,
            String phone,
            String notes,
            Boolean active,
            OffsetDateTime createdAt,
            Long id
    ) {}

    public record ServiceDetails(
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes,
            Boolean active,
            OffsetDateTime createdAt,
            Long id
    ) {}
}
