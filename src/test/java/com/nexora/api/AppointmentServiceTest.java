
package com.nexora.api;

import com.nexora.api.dto.AppointmentResponse;
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
import com.nexora.api.service.AppointmentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentRescheduleHistoryRepository historyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ClientEntity client;

    @Mock
    private ServiceEntity service;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void shouldRejectAppointmentWhenTimeSlotAlreadyExists() {
        Long clientId = 1L;
        Long serviceId = 1L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(client.getActive()).thenReturn(true);
        when(service.getActive()).thenReturn(true);
        when(service.getDurationMinutes()).thenReturn(60);

        when(appointmentRepository
                .existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any()))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRejectAppointmentWhenClientIsInactive() {
        Long clientId = 1L;
        Long serviceId = 1L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(client.getActive()).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(appointmentRepository, never())
                .existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any());

        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRejectAppointmentWhenServiceIsInactive() {
        Long clientId = 1L;
        Long serviceId = 1L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(client.getActive()).thenReturn(true);
        when(service.getActive()).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(appointmentRepository, never())
                .existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any());

        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRejectAppointmentWhenDateIsInThePast() {
        Long clientId = 1L;
        Long serviceId = 1L;
        OffsetDateTime startAt = OffsetDateTime.now().minusDays(1);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(client.getActive()).thenReturn(true);
        when(service.getActive()).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(appointmentRepository, never())
                .existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any());

        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRejectAppointmentWhenClientDoesNotExist() {
        Long clientId = 999L;
        Long serviceId = 1L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(serviceRepository, never()).findById(serviceId);
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRejectAppointmentWhenServiceDoesNotExist() {
        Long clientId = 1L;
        Long serviceId = 999L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.createAppointment(request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldCreateAppointmentAndAssociatedPaymentSuccessfully() {
        Long clientId = 1L;
        Long serviceId = 1L;
        OffsetDateTime startAt = OffsetDateTime.now().plusDays(2);
        BigDecimal price = new BigDecimal("60000.00");

        CreateAppointmentRequest request =
                new CreateAppointmentRequest(clientId, serviceId, startAt);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(client.getActive()).thenReturn(true);
        when(service.getActive()).thenReturn(true);
        when(service.getDurationMinutes()).thenReturn(60);
        when(service.getPrice()).thenReturn(price);

        when(appointmentRepository
                .existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any()))
                .thenReturn(false);

        when(appointmentRepository.save(any(AppointmentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<PaymentEntity> paymentCaptor =
                ArgumentCaptor.forClass(PaymentEntity.class);

        OffsetDateTime beforeCreation = OffsetDateTime.now();

        AppointmentResponse response = appointmentService.createAppointment(request);

        OffsetDateTime afterCreation = OffsetDateTime.now();

        assertNotNull(response);
        assertEquals(startAt, response.startAt());
        assertEquals(startAt.plusMinutes(60), response.endAt());
        assertEquals(60, response.durationMinutes());
        assertEquals(price, response.price());
        assertEquals("PENDING", response.status());

        ArgumentCaptor<AppointmentEntity> appointmentCaptor =
                ArgumentCaptor.forClass(AppointmentEntity.class);

        verify(appointmentRepository).save(appointmentCaptor.capture());

        AppointmentEntity savedAppointment = appointmentCaptor.getValue();

        assertEquals(startAt, savedAppointment.getStartAt());
        assertEquals(startAt.plusMinutes(60), savedAppointment.getEndAt());
        assertEquals(60, savedAppointment.getDurationMinutes());
        assertEquals(price, savedAppointment.getPrice());
        assertEquals(AppointmentStatus.PENDING, savedAppointment.getStatus());

        verify(paymentRepository).save(paymentCaptor.capture());

        PaymentEntity savedPayment = paymentCaptor.getValue();

        assertEquals(savedAppointment, savedPayment.getAppointment());
        assertEquals(price, savedPayment.getAmount());
        assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());

        assertTrue(
                !savedPayment.getPaymentDeadlineAt()
                        .isBefore(beforeCreation.plusHours(48))
                && !savedPayment.getPaymentDeadlineAt()
                        .isAfter(afterCreation.plusHours(48))
        );
    }

    @Test
    void shouldCancelAppointmentAndPendingPayment() {
        Long appointmentId = 1L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.PENDING,
                OffsetDateTime.now().plusDays(5)
        );

        PaymentEntity payment = org.mockito.Mockito.mock(PaymentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(appointmentId);

        assertNotNull(response);
        verify(payment).setStatus(PaymentStatus.CANCELLED);
        verify(paymentRepository).save(payment);
        verify(appointment).setStatus(AppointmentStatus.CANCELLED);
        verify(appointment).setCancelledAt(any(OffsetDateTime.class));
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldCancelAppointmentAndRejectedPayment() {
        Long appointmentId = 11L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.PENDING,
                OffsetDateTime.now().plusDays(5)
        );

        PaymentEntity payment = org.mockito.Mockito.mock(PaymentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.REJECTED);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(appointmentId);

        assertNotNull(response);
        verify(payment).setStatus(PaymentStatus.CANCELLED);
        verify(paymentRepository).save(payment);
        verify(appointment).setStatus(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldCancelAppointmentWhenPaymentDoesNotExist() {
        Long appointmentId = 12L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.PENDING,
                OffsetDateTime.now().plusDays(5)
        );

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.empty());
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(appointmentId);

        assertNotNull(response);
        verify(appointment).setStatus(AppointmentStatus.CANCELLED);
        verify(appointment).setCancelledAt(any(OffsetDateTime.class));
        verify(appointmentRepository).save(appointment);
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }

    @Test
    void shouldRejectCancellationWhenPaymentIsExpired() {
        Long appointmentId = 13L;

        // Mock mínimo: este flujo solo necesita consultar el estado del turno.
        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointment.getStatus()).thenReturn(AppointmentStatus.PENDING);

        PaymentEntity payment = org.mockito.Mockito.mock(PaymentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));
        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(payment));
        when(payment.getStatus()).thenReturn(PaymentStatus.EXPIRED);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(appointmentId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(appointmentRepository, never()).save(appointment);
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void shouldMarkApprovedPaymentAppointmentAsRescheduleEligibleWhenCancelledWithinDeadline() {
        Long appointmentId = 2L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.CONFIRMED,
                OffsetDateTime.now().plusDays(5)
        );

        PaymentEntity payment = org.mockito.Mockito.mock(PaymentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus()).thenReturn(PaymentStatus.APPROVED);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(appointmentId);

        assertNotNull(response);
        verify(appointment).setStatus(AppointmentStatus.RESCHEDULE_ELIGIBLE);
        verify(appointment).setCancelledAt(any(OffsetDateTime.class));
        verify(appointmentRepository).save(appointment);
        verify(payment, never()).setStatus(any());
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void shouldCancelAppointmentWithoutChangingSubmittedPayment() {
        Long appointmentId = 5L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.CONFIRMED,
                OffsetDateTime.now().plusDays(5)
        );

        PaymentEntity payment = org.mockito.Mockito.mock(PaymentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus()).thenReturn(PaymentStatus.SUBMITTED);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(appointmentId);

        assertNotNull(response);

        verify(appointment).setStatus(AppointmentStatus.CANCELLED);
        verify(appointment).setCancelledAt(any(OffsetDateTime.class));
        verify(appointmentRepository).save(appointment);

        verify(payment, never()).setStatus(any());
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void shouldMarkApprovedPaymentAppointmentAsLostWhenCancelledOutsideDeadline() {
        Long appointmentId = 6L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.CONFIRMED,
                OffsetDateTime.now().plusDays(1)
        );

        PaymentEntity payment = org.mockito.Mockito.mock(PaymentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.findByAppointmentId(appointmentId))
                .thenReturn(Optional.of(payment));

        when(payment.getStatus()).thenReturn(PaymentStatus.APPROVED);
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(appointmentId);

        assertNotNull(response);

        verify(appointment).setStatus(AppointmentStatus.LOST);
        verify(appointment).setCancelledAt(any(OffsetDateTime.class));
        verify(appointmentRepository).save(appointment);

        verify(payment, never()).setStatus(any());
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void shouldRejectCancellationWhenAppointmentIsInNonCancellableStatus() {
        Long appointmentId = 7L;

        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointment.getStatus()).thenReturn(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(appointmentId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(paymentRepository, never()).findByAppointmentId(appointmentId);
        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void shouldRejectCancellationWhenAppointmentDoesNotExist() {
        Long appointmentId = 999L;

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.cancelAppointment(appointmentId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(paymentRepository, never()).findByAppointmentId(appointmentId);
        verify(appointmentRepository, never()).save(any(AppointmentEntity.class));
    }

    @Test
    void shouldRescheduleAppointmentAndSaveHistory() {
        Long appointmentId = 3L;

        OffsetDateTime previousStartAt = OffsetDateTime.now().plusDays(2);
        OffsetDateTime previousEndAt = previousStartAt.plusMinutes(60);
        OffsetDateTime newStartAt = OffsetDateTime.now().plusDays(4);
        OffsetDateTime newEndAt = newStartAt.plusMinutes(60);

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.PENDING,
                previousStartAt
        );

        when(appointment.getEndAt()).thenReturn(previousEndAt);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any(), any()))
                .thenReturn(false);

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        RescheduleAppointmentRequest request =
                new RescheduleAppointmentRequest(newStartAt);

        AppointmentResponse response =
                appointmentService.rescheduleAppointment(appointmentId, request);

        assertNotNull(response);

        ArgumentCaptor<AppointmentRescheduleHistoryEntity> historyCaptor =
                ArgumentCaptor.forClass(AppointmentRescheduleHistoryEntity.class);

        verify(historyRepository).save(historyCaptor.capture());

        AppointmentRescheduleHistoryEntity savedHistory = historyCaptor.getValue();

        assertEquals(appointment, savedHistory.getAppointment());
        assertEquals(previousStartAt, savedHistory.getPreviousStartAt());
        assertEquals(previousEndAt, savedHistory.getPreviousEndAt());
        assertEquals(newStartAt, savedHistory.getNewStartAt());
        assertEquals(newEndAt, savedHistory.getNewEndAt());
        assertNotNull(savedHistory.getRescheduledAt());

        verify(appointment).setStartAt(newStartAt);
        verify(appointment).setEndAt(newEndAt);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldRejectRescheduleWhenNewTimeSlotOverlapsAnotherAppointment() {
        Long appointmentId = 4L;

        OffsetDateTime newStartAt = OffsetDateTime.now().plusDays(4);

        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointment.getStatus()).thenReturn(AppointmentStatus.PENDING);
        when(appointment.getDurationMinutes()).thenReturn(60);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any(), any()))
                .thenReturn(true);

        RescheduleAppointmentRequest request =
                new RescheduleAppointmentRequest(newStartAt);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(appointmentId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(historyRepository, never())
                .save(any(AppointmentRescheduleHistoryEntity.class));

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void shouldRejectRescheduleWhenNewDateIsInThePast() {
        Long appointmentId = 8L;

        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointment.getStatus()).thenReturn(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        RescheduleAppointmentRequest request =
                new RescheduleAppointmentRequest(OffsetDateTime.now().minusDays(1));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(appointmentId, request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(appointmentRepository, never())
                .existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any(), any());

        verify(historyRepository, never())
                .save(any(AppointmentRescheduleHistoryEntity.class));

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void shouldRejectRescheduleWhenAppointmentIsInNonReschedulableStatus() {
        Long appointmentId = 9L;

        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointment.getStatus()).thenReturn(AppointmentStatus.CANCELLED);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        RescheduleAppointmentRequest request =
                new RescheduleAppointmentRequest(OffsetDateTime.now().plusDays(4));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> appointmentService.rescheduleAppointment(appointmentId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(appointmentRepository, never())
                .existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any(), any());

        verify(historyRepository, never())
                .save(any(AppointmentRescheduleHistoryEntity.class));

        verify(appointmentRepository, never()).save(appointment);
    }

    @Test
    void shouldConfirmAppointmentAndClearCancellationWhenReschedulingEligibleAppointment() {
        Long appointmentId = 10L;

        OffsetDateTime previousStartAt = OffsetDateTime.now().plusDays(2);
        OffsetDateTime newStartAt = OffsetDateTime.now().plusDays(4);

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.RESCHEDULE_ELIGIBLE,
                previousStartAt
        );

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(appointmentRepository
                .existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
                        any(), any(), any(), any()))
                .thenReturn(false);

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        RescheduleAppointmentRequest request =
                new RescheduleAppointmentRequest(newStartAt);

        AppointmentResponse response =
                appointmentService.rescheduleAppointment(appointmentId, request);

        assertNotNull(response);

        verify(appointment).setStatus(AppointmentStatus.CONFIRMED);
        verify(appointment).setCancelledAt(null);
        verify(appointment).setStartAt(newStartAt);
        verify(appointment).setEndAt(newStartAt.plusMinutes(60));

        verify(historyRepository).save(any(AppointmentRescheduleHistoryEntity.class));
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldReturnAllAppointments() {
        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.PENDING,
                OffsetDateTime.now().plusDays(2)
        );

        when(appointmentRepository.findAll())
                .thenReturn(List.of(appointment));

        List<AppointmentResponse> responses =
                appointmentService.getAllAppointments();

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());
        assertEquals("PENDING", responses.get(0).status());
    }

    @Test
    void shouldReturnAppointmentById() {
        Long appointmentId = 1L;

        AppointmentEntity appointment = createAppointmentMock(
                AppointmentStatus.PENDING,
                OffsetDateTime.now().plusDays(2)
        );

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        AppointmentResponse response =
                appointmentService.getAppointmentById(appointmentId);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("PENDING", response.status());
    }

    @Test
    void shouldReturnEmptyRescheduleHistoryWhenNoHistoryExists() {
        Long appointmentId = 1L;

        // Mock mínimo: el servicio solo necesita validar que el turno exista.
        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointmentRepository.findById(appointmentId))
                .thenReturn(Optional.of(appointment));

        when(historyRepository.findByAppointmentIdOrderByRescheduledAtDesc(
                appointmentId))
                .thenReturn(List.of());

        List<com.nexora.api.dto.AppointmentRescheduleHistoryResponse> history =
                appointmentService.getRescheduleHistory(appointmentId);

        assertNotNull(history);
        assertTrue(history.isEmpty());
        verify(historyRepository)
                .findByAppointmentIdOrderByRescheduledAtDesc(appointmentId);
    }

    private AppointmentEntity createAppointmentMock(
            AppointmentStatus status,
            OffsetDateTime startAt) {

        AppointmentEntity appointment =
                org.mockito.Mockito.mock(AppointmentEntity.class);

        when(appointment.getClient()).thenReturn(client);
        when(appointment.getService()).thenReturn(service);
        when(appointment.getStartAt()).thenReturn(startAt);
        when(appointment.getEndAt()).thenReturn(startAt.plusMinutes(60));
        when(appointment.getDurationMinutes()).thenReturn(60);
        when(appointment.getPrice()).thenReturn(new BigDecimal("60000.00"));
        when(appointment.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(appointment.getId()).thenReturn(1L);
        when(appointment.getStatus()).thenReturn(status);

        return appointment;
    }
}
