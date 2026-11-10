
package com.nexora.api.service;

import com.nexora.api.dto.PaymentResponse;
import com.nexora.api.dto.SubmitPaymentReceiptRequest;
import com.nexora.api.dto.ReviewPaymentRequest;
import com.nexora.api.entity.AppointmentEntity;
import com.nexora.api.entity.PaymentEntity;
import com.nexora.api.enums.AppointmentStatus;
import com.nexora.api.enums.PaymentStatus;
import com.nexora.api.repository.AppointmentRepository;
import com.nexora.api.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            AppointmentRepository appointmentRepository) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pago no encontrado"));

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByAppointmentId(Long appointmentId) {
        PaymentEntity payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró un pago para ese turno"));

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse submitReceipt(
            Long paymentId,
            SubmitPaymentReceiptRequest request) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pago no encontrado"));

        PaymentStatus status = payment.getStatus();

        if (status != PaymentStatus.PENDING
                && status != PaymentStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede enviar un comprobante para este pago");
        }

        if (payment.getAppointment().getStatus()
                != AppointmentStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede enviar un comprobante para un turno cancelado o cerrado");
        }

        boolean deadlinePassed = !payment.getPaymentDeadlineAt()
                .isAfter(OffsetDateTime.now());

        if (deadlinePassed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El plazo de 48 horas para enviar el comprobante venció");
        }

        payment.setReceiptReference(request.receiptReference().trim());
        payment.setSubmittedAt(OffsetDateTime.now());
        payment.setReviewedAt(null);
        payment.setReviewNotes(null);
        payment.setStatus(PaymentStatus.SUBMITTED);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        return PaymentResponse.from(savedPayment);
    }

    @Transactional
    public PaymentResponse approvePayment(
            Long paymentId,
            ReviewPaymentRequest request) {

        PaymentEntity payment = getPaymentForReview(paymentId);
        AppointmentEntity appointment = payment.getAppointment();

        if (appointment.getStatus() == AppointmentStatus.PENDING) {

            appointment.setStatus(AppointmentStatus.CONFIRMED);

        } else if (appointment.getStatus() == AppointmentStatus.CANCELLED) {

            OffsetDateTime cancelledAt = appointment.getCancelledAt();

            if (cancelledAt == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se puede determinar la anticipación de la cancelación");
            }

            OffsetDateTime rescheduleLimit =
                    appointment.getStartAt().minusHours(48);

            if (!cancelledAt.isAfter(rescheduleLimit)) {
                appointment.setStatus(
                        AppointmentStatus.RESCHEDULE_ELIGIBLE
                );
            } else {
                appointment.setStatus(AppointmentStatus.LOST);
            }

        } else {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede aprobar el pago porque el turno está en un estado incompatible");
        }

        payment.setStatus(PaymentStatus.APPROVED);
        payment.setReviewedAt(OffsetDateTime.now());
        payment.setReviewNotes(request.reviewNotes());

        appointmentRepository.save(appointment);

        PaymentEntity savedPayment = paymentRepository.save(payment);

        return PaymentResponse.from(savedPayment);
    }

    @Transactional
    public PaymentResponse rejectPayment(
            Long paymentId,
            ReviewPaymentRequest request) {

        PaymentEntity payment = getPaymentForReview(paymentId);
        AppointmentEntity appointment = payment.getAppointment();

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede rechazar el comprobante porque el turno está en un estado incompatible");
        }

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setReviewedAt(OffsetDateTime.now());
        payment.setReviewNotes(request.reviewNotes());

        PaymentEntity savedPayment = paymentRepository.save(payment);

        return PaymentResponse.from(savedPayment);
    }

    private PaymentEntity getPaymentForReview(Long paymentId) {

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pago no encontrado"));

        if (payment.getStatus() != PaymentStatus.SUBMITTED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden revisar pagos con comprobantes enviados");
        }

        return payment;
    }
}
