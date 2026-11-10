
package com.nexora.api.repository;

import com.nexora.api.entity.AppointmentEntity;
import com.nexora.api.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    boolean existsByStatusInAndStartAtLessThanAndEndAtGreaterThan(
            List<AppointmentStatus> statuses,
            OffsetDateTime requestedEnd,
            OffsetDateTime requestedStart
    );

    boolean existsByStatusInAndIdNotAndStartAtLessThanAndEndAtGreaterThan(
            List<AppointmentStatus> statuses,
            Long appointmentId,
            OffsetDateTime requestedEnd,
            OffsetDateTime requestedStart
    );
}
