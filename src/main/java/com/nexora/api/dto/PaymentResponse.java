package com.nexora.api.dto;

import com.nexora.api.entity.PaymentEntity;
import com.nexora.api.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long appointmentId,
        BigDecimal amount,
        PaymentStatus status,
        String receiptReference,
        OffsetDateTime paymentDeadlineAt,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        String reviewNotes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PaymentResponse from(PaymentEntity payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getAppointment().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getReceiptReference(),
                payment.getPaymentDeadlineAt(),
                payment.getSubmittedAt(),
                payment.getReviewedAt(),
                payment.getReviewNotes(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
