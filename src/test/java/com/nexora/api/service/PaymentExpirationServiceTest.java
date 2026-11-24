
package com.nexora.api.service;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentExpirationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PaymentExpirationService paymentExpirationService;

    @Test
    void shouldExpireOverduePendingPaymentAndAppointment() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.PENDING
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.PENDING,
                OffsetDateTime.now().minusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        assertEquals(AppointmentStatus.EXPIRED, appointment.getStatus());

        verify(paymentRepository).save(payment);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldExpireOverdueRejectedPaymentAndAppointment() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.PENDING
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.REJECTED,
                OffsetDateTime.now().minusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        assertEquals(AppointmentStatus.EXPIRED, appointment.getStatus());

        verify(paymentRepository).save(payment);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldNotExpirePaymentBeforeDeadline() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.PENDING
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.PENDING,
                OffsetDateTime.now().plusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());

        verify(paymentRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldNotExpireApprovedPayment() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.PENDING
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.APPROVED,
                OffsetDateTime.now().minusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());

        verify(paymentRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldNotExpireSubmittedPayment() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.PENDING
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.SUBMITTED,
                OffsetDateTime.now().minusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.SUBMITTED, payment.getStatus());
        assertEquals(AppointmentStatus.PENDING, appointment.getStatus());

        verify(paymentRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldNotChangeCancelledAppointmentWhenPaymentExpires() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.CANCELLED
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.PENDING,
                OffsetDateTime.now().minusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());

        verify(paymentRepository).save(payment);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldNotChangeAlreadyExpiredAppointmentWhenPaymentExpires() {
        AppointmentEntity appointment = createAppointment(
                AppointmentStatus.EXPIRED
        );

        PaymentEntity payment = createPayment(
                appointment,
                PaymentStatus.PENDING,
                OffsetDateTime.now().minusDays(1)
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        paymentExpirationService.expireOverduePayments();

        assertEquals(PaymentStatus.EXPIRED, payment.getStatus());
        assertEquals(AppointmentStatus.EXPIRED, appointment.getStatus());

        verify(paymentRepository).save(payment);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldFinishWithoutChangesWhenThereAreNoPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        paymentExpirationService.expireOverduePayments();

        verify(paymentRepository).findAll();
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(appointmentRepository);
    }

    private AppointmentEntity createAppointment(AppointmentStatus status) {
        OffsetDateTime startAt = OffsetDateTime.now();

        AppointmentEntity appointment = new AppointmentEntity(
                null,
                null,
                startAt,
                startAt.plusMinutes(60),
                60,
                new BigDecimal("50000.00")
        );

        appointment.setStatus(status);

        return appointment;
    }

    private PaymentEntity createPayment(
            AppointmentEntity appointment,
            PaymentStatus status,
            OffsetDateTime deadline) {

        PaymentEntity payment = new PaymentEntity(
                appointment,
                new BigDecimal("50000.00"),
                deadline
        );

        payment.setStatus(status);

        return payment;
    }
}
