package com.nexora.api.dto;

import com.nexora.api.entity.AppointmentRescheduleHistoryEntity;
import java.time.OffsetDateTime;

public record AppointmentRescheduleHistoryResponse(
        Long id,
        Long appointmentId,
        OffsetDateTime previousStartAt,
        OffsetDateTime previousEndAt,
        OffsetDateTime newStartAt,
        OffsetDateTime newEndAt,
        OffsetDateTime rescheduledAt
) {

    public static AppointmentRescheduleHistoryResponse from(
            AppointmentRescheduleHistoryEntity history) {

        return new AppointmentRescheduleHistoryResponse(
                history.getId(),
                history.getAppointment().getId(),
                history.getPreviousStartAt(),
                history.getPreviousEndAt(),
                history.getNewStartAt(),
                history.getNewEndAt(),
                history.getRescheduledAt()
        );
    }
}