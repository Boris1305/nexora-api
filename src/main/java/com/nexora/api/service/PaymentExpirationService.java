
package com.nexora.api.service;

import com.nexora.api.entity.AppointmentEntity;
import com.nexora.api.entity.PaymentEntity;
import com.nexora.api.enums.AppointmentStatus;
import com.nexora.api.enums.PaymentStatus;
import com.nexora.api.repository.AppointmentRepository;
import com.nexora.api.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PaymentExpirationService {

    private static final Logger logger =
            LoggerFactory.getLogger(PaymentExpirationService.class);

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    public PaymentExpirationService(
            PaymentRepository paymentRepository,
            AppointmentRepository appointmentRepository) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    @Transactional
    public void expireOverduePayments() {

        logger.info("Iniciando revisión automática de vencimientos");

        OffsetDateTime now = OffsetDateTime.now();
        List<PaymentEntity> payments = paymentRepository.findAll();

        int expiredCount = 0;

        for (PaymentEntity payment : payments) {

            boolean canExpire =
                    payment.getStatus() == PaymentStatus.PENDING
                    || payment.getStatus() == PaymentStatus.REJECTED;

            boolean deadlinePassed =
                    !payment.getPaymentDeadlineAt().isAfter(now);

            if (canExpire && deadlinePassed) {

                payment.setStatus(PaymentStatus.EXPIRED);
                paymentRepository.save(payment);

                AppointmentEntity appointment = payment.getAppointment();

                if (appointment.getStatus() == AppointmentStatus.PENDING) {
                    appointment.setStatus(AppointmentStatus.EXPIRED);
                    appointmentRepository.save(appointment);
                }

                expiredCount++;

                logger.info("Pago {} vencido automáticamente", payment.getId());
            }
        }

        logger.info(
                "Revisión finalizada. Pagos vencidos: {}",
                expiredCount
        );
    }
}
