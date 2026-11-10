
package com.nexora.api.service;

import com.nexora.api.dto.AppointmentResponse;
import com.nexora.api.dto.AppointmentRescheduleHistoryResponse;
import com.nexora.api.dto.CreateAppointmentRequest;
import com.nexora.api.dto.RescheduleAppointmentRequest;
import com.nexora.api.entity.AppointmentEntity;
import com.nexora.api.entity.AppointmentRescheduleHistoryEntity;
import com.nexora.api.entity.ClientEntity;
import com.nexora.api.entity.PaymentEntity;
import com.nexora.api.entity.ServiceEntity;
import com.nexora.api.enums.AppointmentStatus;
import com.nexora.api.enums.PaymentStatus;
import com.nexora.api.repository.AppointmentRepository;
import com.nexora.api.repository.AppointmentRescheduleHistoryRepository;
import com.nexora.api.repository.ClientRepository;
import com.nexora.api.repository.PaymentRepository;
import com.nexora.api.repository.ServiceRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentRescheduleHistoryRepository historyRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final PaymentRepository paymentRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentRescheduleHistoryRepository historyRepository,
            ClientRepository clientRepository,
            PaymentRepository paymentRepository,
            ServiceRepository serviceRepository) {

        this.appointmentRepository = appointmentRepository;
        this.historyRepository = historyRepository;
        this.clientRepository = clientRepository;
        this.paymentRepository = paymentRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        AppointmentEntity appointment = findAppointmentEntityById(id);
        return AppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentRescheduleHistoryResponse> getRescheduleHistory(
            Long appointmentId) {

        findAppointmentEntityById(appointmentId);

        List<AppointmentRescheduleHistoryEntity> history =
                historyRepository.findByAppointmentIdOrderByRescheduledAtDesc(
                        appointmentId
                );

        return history.stream()
                .map(AppointmentRescheduleHistoryResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {

        ClientEntity client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        ServiceEntity service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Servicio no encontrado"));

        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El cliente está inactivo");
        }

        if (!Boolean.TRUE.equals(service.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El servicio está inactivo");
        }

        OffsetDateTime startAt = request.startAt();

        if (!startAt.isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El turno debe tener una fecha futura");
        }

        Integer durationMinutes = service.getDurationMinutes();
        OffsetDateTime endAt = startAt.plusMinutes(durationMinutes);

        boolean overlaps = appointmentRepository
                .existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        List.of(
                                AppointmentStatus.PENDING,
                                AppointmentStatus.CONFIRMED
                        ),
                        endAt,
                        startAt
                );

        if (overlaps) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un turno en ese horario");
        }

        AppointmentEntity appointment = new AppointmentEntity(
                client,
                service,
                startAt,
                endAt,
                durationMinutes,
                service.getPrice()
        );

        AppointmentEntity savedAppointment =
                appointmentRepository.save(appointment);

        OffsetDateTime paymentDeadline =
                OffsetDateTime.now().plusHours(48);

        PaymentEntity payment = new PaymentEntity(
                savedAppointment,
                service.getPrice(),
                paymentDeadline
        );

        paymentRepository.save(payment);

        return AppointmentResponse.from(savedAppointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id) {

        AppointmentEntity appointment = findAppointmentEntityById(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cancelar un turno en este estado"
            );
        }

        OffsetDateTime cancelledAt = OffsetDateTime.now();

        PaymentEntity payment = paymentRepository
                .findByAppointmentId(id)
                .orElse(null);

        AppointmentStatus newAppointmentStatus =
                AppointmentStatus.CANCELLED;

        if (payment != null) {

            PaymentStatus paymentStatus = payment.getStatus();

            if (paymentStatus == PaymentStatus.PENDING
                    || paymentStatus == PaymentStatus.REJECTED) {

                payment.setStatus(PaymentStatus.CANCELLED);
                paymentRepository.save(payment);

            } else if (paymentStatus == PaymentStatus.SUBMITTED) {

                // El comprobante queda pendiente de revisión.
                // El turno se cancela y el horario queda libre.

            } else if (paymentStatus == PaymentStatus.APPROVED) {

                OffsetDateTime rescheduleLimit =
                        appointment.getStartAt().minusHours(48);

                if (!cancelledAt.isAfter(rescheduleLimit)) {
                    newAppointmentStatus =
                            AppointmentStatus.RESCHEDULE_ELIGIBLE;
                } else {
                    newAppointmentStatus = AppointmentStatus.LOST;
                }

            } else {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No se puede cancelar un turno con un pago en estado "
                                + paymentStatus
                );
            }
        }

        appointment.setCancelledAt(cancelledAt);
        appointment.setStatus(newAppointmentStatus);

        AppointmentEntity savedAppointment =
                appointmentRepository.save(appointment);

        return AppointmentResponse.from(savedAppointment);
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(
            Long id,
            RescheduleAppointmentRequest request) {

        AppointmentEntity appointment = findAppointmentEntityById(id);

        if (appointment.getStatus() != AppointmentStatus.PENDING
                && appointment.getStatus() != AppointmentStatus.CONFIRMED
                && appointment.getStatus() != AppointmentStatus.RESCHEDULE_ELIGIBLE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede reprogramar un turno en este estado"
            );
        }

        OffsetDateTime newStartAt = request.newStartAt();

        if (!newStartAt.isAfter(OffsetDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva fecha debe ser futura"
            );
        }

        Integer durationMinutes = appointment.getDurationMinutes();
        OffsetDateTime newEndAt = newStartAt.plusMinutes(durationMinutes);

        boolean overlaps = appointmentRepository
                .existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                        List.of(
                                AppointmentStatus.PENDING,
                                AppointmentStatus.CONFIRMED
                        ),
                        id,
                        newEndAt,
                        newStartAt
                );

        if (overlaps) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro turno en ese horario"
            );
        }

        OffsetDateTime previousStartAt = appointment.getStartAt();
        OffsetDateTime previousEndAt = appointment.getEndAt();

        AppointmentRescheduleHistoryEntity history =
                new AppointmentRescheduleHistoryEntity(
                        appointment,
                        previousStartAt,
                        previousEndAt,
                        newStartAt,
                        newEndAt
                );

        historyRepository.save(history);

        appointment.setStartAt(newStartAt);
        appointment.setEndAt(newEndAt);

        if (appointment.getStatus() == AppointmentStatus.RESCHEDULE_ELIGIBLE) {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            appointment.setCancelledAt(null);
        }

        AppointmentEntity savedAppointment =
                appointmentRepository.save(appointment);

        return AppointmentResponse.from(savedAppointment);
    }

    private AppointmentEntity findAppointmentEntityById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Turno no encontrado"
                ));
    }
}
