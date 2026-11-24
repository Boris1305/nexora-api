
package com.nexora.api.service;

import com.nexora.api.dto.PaymentResponse;
import com.nexora.api.dto.ReviewPaymentRequest;
import com.nexora.api.dto.SubmitPaymentReceiptRequest;
import com.nexora.api.entity.AppointmentEntity;
import com.nexora.api.entity.PaymentEntity;
import com.nexora.api.enums.AppointmentStatus;
import com.nexora.api.enums.PaymentStatus;
import com.nexora.api.repository.AppointmentRepository;
import com.nexora.api.repository.PaymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceAdditionalTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private AppointmentEntity createAppointment(
            AppointmentStatus status) {

        AppointmentEntity appointment = mock(AppointmentEntity.class);

        lenient().when(appointment.getId()).thenReturn(1L);
        lenient().when(appointment.getStatus()).thenReturn(status);

        return appointment;
    }

    private PaymentEntity createPayment(
            AppointmentEntity appointment,
            PaymentStatus status) {

        PaymentEntity payment = mock(PaymentEntity.class);

        lenient().when(payment.getId()).thenReturn(1L);
        lenient().when(payment.getAppointment()).thenReturn(appointment);
        lenient().when(payment.getAmount())
                .thenReturn(new BigDecimal("50000.00"));
        lenient().when(payment.getStatus()).thenReturn(status);
        lenient().when(payment.getReceiptReference())
                .thenReturn("receipt-123");
        lenient().when(payment.getPaymentDeadlineAt())
                .thenReturn(OffsetDateTime.now().plusHours(24));
        lenient().when(payment.getSubmittedAt())
                .thenReturn(OffsetDateTime.now());
        lenient().when(payment.getReviewedAt()).thenReturn(null);
        lenient().when(payment.getReviewNotes()).thenReturn(null);
        lenient().when(payment.getCreatedAt())
                .thenReturn(OffsetDateTime.now());
        lenient().when(payment.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());

        return payment;
    }

    @Test
    void shouldResubmitReceiptWhenPaymentWasRejected() {

        AppointmentEntity appointment =
                createAppointment(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPayment(appointment, PaymentStatus.REJECTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        PaymentResponse response = paymentService.submitReceipt(
                1L,
                new SubmitPaymentReceiptRequest("  receipt-new  ")
        );

        assertNotNull(response);

        verify(payment).setReceiptReference("receipt-new");
        verify(payment).setStatus(PaymentStatus.SUBMITTED);
        verify(payment).setSubmittedAt(any(OffsetDateTime.class));
        verify(payment).setReviewedAt(null);
        verify(payment).setReviewNotes(null);
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldRejectPaymentWhenAppointmentIsCancelled() {

        AppointmentEntity appointment =
                createAppointment(AppointmentStatus.CANCELLED);

        PaymentEntity payment =
                createPayment(appointment, PaymentStatus.SUBMITTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        PaymentResponse response = paymentService.rejectPayment(
                1L,
                new ReviewPaymentRequest("Comprobante ilegible")
        );

        assertNotNull(response);

        verify(payment).setStatus(PaymentStatus.REJECTED);
        verify(payment).setReviewedAt(any(OffsetDateTime.class));
        verify(payment).setReviewNotes("Comprobante ilegible");
        verify(paymentRepository).save(payment);

        verify(appointmentRepository, never()).save(any());
    }
}
