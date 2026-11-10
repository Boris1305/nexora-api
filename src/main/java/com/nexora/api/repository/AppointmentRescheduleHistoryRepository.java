
package com.nexora.api.repository;

import com.nexora.api.entity.AppointmentRescheduleHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRescheduleHistoryRepository
        extends JpaRepository<AppointmentRescheduleHistoryEntity, Long> {

    List<AppointmentRescheduleHistoryEntity>
            findByAppointmentIdOrderByRescheduledAtDesc(Long appointmentId);
}
