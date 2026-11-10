package com.nexora.api.repository;

import com.nexora.api.entity.PaymentEntity;
import com.nexora.api.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByAppointmentId(Long appointmentId);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    boolean existsByAppointmentId(Long appointmentId);
}