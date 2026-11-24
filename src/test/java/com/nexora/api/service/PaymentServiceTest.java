
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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentEntity createPaymentMock(
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

    private AppointmentEntity createAppointmentMock(
            AppointmentStatus status) {

        AppointmentEntity appointment = mock(AppointmentEntity.class);

        lenient().when(appointment.getId()).thenReturn(1L);
        lenient().when(appointment.getStatus()).thenReturn(status);

        return appointment;
    }

    private void stubPaymentResponse(
            PaymentEntity payment,
            AppointmentEntity appointment) {

        lenient().when(payment.getId()).thenReturn(1L);
        lenient().when(payment.getAppointment()).thenReturn(appointment);
        lenient().when(appointment.getId()).thenReturn(1L);
        lenient().when(payment.getAmount())
                .thenReturn(new BigDecimal("50000.00"));
        lenient().when(payment.getReceiptReference())
                .thenReturn("receipt-123");
        lenient().when(payment.getPaymentDeadlineAt())
                .thenReturn(OffsetDateTime.now().plusHours(24));
        lenient().when(payment.getSubmittedAt())
                .thenReturn(OffsetDateTime.now());
        lenient().when(payment.getReviewedAt())
                .thenReturn(OffsetDateTime.now());
        lenient().when(payment.getReviewNotes())
                .thenReturn("Comprobante revisado");
        lenient().when(payment.getCreatedAt())
                .thenReturn(OffsetDateTime.now());
        lenient().when(payment.getUpdatedAt())
                .thenReturn(OffsetDateTime.now());
    }

    private void assertStatus(
            ResponseStatusException exception,
            HttpStatus expectedStatus) {

        assertEquals(expectedStatus, exception.getStatusCode());
    }

    // GET PAYMENT BY ID

    @Test
    void shouldGetPaymentById() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.PENDING);

        stubPaymentResponse(payment, appointment);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(paymentRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenPaymentDoesNotExist() {

        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.getPaymentById(999L));

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    // GET PAYMENT BY APPOINTMENT ID

    @Test
    void shouldGetPaymentByAppointmentId() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.PENDING);

        stubPaymentResponse(payment, appointment);

        when(paymentRepository.findByAppointmentId(1L))
                .thenReturn(Optional.of(payment));

        PaymentResponse response =
                paymentService.getPaymentByAppointmentId(1L);

        assertNotNull(response);
        assertEquals(1L, response.appointmentId());
    }

    @Test
    void shouldThrowNotFoundWhenAppointmentHasNoPayment() {

        when(paymentRepository.findByAppointmentId(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.getPaymentByAppointmentId(999L));

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    // SUBMIT RECEIPT

    @Test
    void shouldSubmitReceiptSuccessfully() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.PENDING);

        stubPaymentResponse(payment, appointment);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        SubmitPaymentReceiptRequest request =
                new SubmitPaymentReceiptRequest("  receipt-456  ");

        PaymentResponse response =
                paymentService.submitReceipt(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.id());

        verify(payment).setReceiptReference("receipt-456");
        verify(payment).setStatus(PaymentStatus.SUBMITTED);
        verify(payment).setSubmittedAt(any(OffsetDateTime.class));
        verify(payment).setReviewedAt(null);
        verify(payment).setReviewNotes(null);
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldRejectReceiptWhenPaymentIsNotPendingOrRejected() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.submitReceipt(
                        1L,
                        new SubmitPaymentReceiptRequest("receipt-456")));

        assertStatus(exception, HttpStatus.CONFLICT);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldRejectReceiptWhenAppointmentIsNotPending() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.CANCELLED);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.PENDING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.submitReceipt(
                        1L,
                        new SubmitPaymentReceiptRequest("receipt-456")));

        assertStatus(exception, HttpStatus.CONFLICT);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldRejectReceiptWhenPaymentDeadlineHasPassed() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.PENDING);

        when(payment.getPaymentDeadlineAt())
                .thenReturn(OffsetDateTime.now().minusMinutes(1));

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.submitReceipt(
                        1L,
                        new SubmitPaymentReceiptRequest("receipt-456")));

        assertStatus(exception, HttpStatus.CONFLICT);
        verify(paymentRepository, never()).save(any());
    }

    // APPROVE PAYMENT

    @Test
    void shouldApprovePaymentAndConfirmAppointment() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        stubPaymentResponse(payment, appointment);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        PaymentResponse response = paymentService.approvePayment(
                1L,
                new ReviewPaymentRequest("Pago verificado"));

        assertNotNull(response);

        verify(appointment).setStatus(AppointmentStatus.CONFIRMED);
        verify(payment).setStatus(PaymentStatus.APPROVED);
        verify(payment).setReviewedAt(any(OffsetDateTime.class));
        verify(payment).setReviewNotes("Pago verificado");

        verify(appointmentRepository).save(appointment);
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldMarkCancelledAppointmentAsRescheduleEligibleWhenApprovedWithinDeadline() {

        OffsetDateTime now = OffsetDateTime.now();

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.CANCELLED);

        when(appointment.getCancelledAt())
                .thenReturn(now.minusHours(72));

        when(appointment.getStartAt())
                .thenReturn(now.plusHours(120));

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        stubPaymentResponse(payment, appointment);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        paymentService.approvePayment(
                1L,
                new ReviewPaymentRequest("Pago aprobado"));

        verify(appointment).setStatus(
                AppointmentStatus.RESCHEDULE_ELIGIBLE);

        verify(appointmentRepository).save(appointment);
        verify(payment).setStatus(PaymentStatus.APPROVED);
    }

    @Test
    void shouldMarkCancelledAppointmentAsLostWhenApprovedOutsideDeadline() {

        OffsetDateTime now = OffsetDateTime.now();

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.CANCELLED);

        when(appointment.getCancelledAt())
                .thenReturn(now.minusHours(12));

        when(appointment.getStartAt())
                .thenReturn(now.plusHours(24));

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        paymentService.approvePayment(
                1L,
                new ReviewPaymentRequest("Pago aprobado"));

        verify(appointment).setStatus(AppointmentStatus.LOST);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldRejectApprovalWhenAppointmentHasIncompatibleStatus() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.CONFIRMED);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.approvePayment(
                        1L,
                        new ReviewPaymentRequest("Revisión")));

        assertStatus(exception, HttpStatus.CONFLICT);

        verify(paymentRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }

    // REJECT PAYMENT

    @Test
    void shouldRejectPaymentSuccessfully() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        stubPaymentResponse(payment, appointment);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        PaymentResponse response = paymentService.rejectPayment(
                1L,
                new ReviewPaymentRequest("Comprobante ilegible"));

        assertNotNull(response);

        verify(payment).setStatus(PaymentStatus.REJECTED);
        verify(payment).setReviewedAt(any(OffsetDateTime.class));
        verify(payment).setReviewNotes("Comprobante ilegible");
        verify(paymentRepository).save(payment);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectPaymentWhenAppointmentHasIncompatibleStatus() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.CONFIRMED);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.rejectPayment(
                        1L,
                        new ReviewPaymentRequest("Comprobante ilegible")));

        assertStatus(exception, HttpStatus.CONFLICT);
        verify(paymentRepository, never()).save(any());
    }

    // REVIEW VALIDATIONS

    @Test
    void shouldRejectReviewWhenPaymentIsNotSubmitted() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.PENDING);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.PENDING);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.approvePayment(
                        1L,
                        new ReviewPaymentRequest("Revisión")));

        assertStatus(exception, HttpStatus.CONFLICT);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void shouldRejectReviewWhenPaymentDoesNotExist() {

        when(paymentRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.approvePayment(
                        999L,
                        new ReviewPaymentRequest("Revisión")));

        assertStatus(exception, HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldRejectApprovalWhenCancelledAtIsMissing() {

        AppointmentEntity appointment =
                createAppointmentMock(AppointmentStatus.CANCELLED);

        when(appointment.getCancelledAt()).thenReturn(null);

        PaymentEntity payment =
                createPaymentMock(appointment, PaymentStatus.SUBMITTED);

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.approvePayment(
                        1L,
                        new ReviewPaymentRequest("Revisión")));

        assertStatus(exception, HttpStatus.CONFLICT);
        verify(paymentRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
    }
}
