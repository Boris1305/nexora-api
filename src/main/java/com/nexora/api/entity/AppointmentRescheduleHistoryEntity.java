
package com.nexora.api.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "appointment_reschedule_history")
public class AppointmentRescheduleHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @Column(name = "previous_start_at", nullable = false)
    private OffsetDateTime previousStartAt;

    @Column(name = "previous_end_at", nullable = false)
    private OffsetDateTime previousEndAt;

    @Column(name = "new_start_at", nullable = false)
    private OffsetDateTime newStartAt;

    @Column(name = "new_end_at", nullable = false)
    private OffsetDateTime newEndAt;

    @Column(name = "rescheduled_at", nullable = false, updatable = false)
    private OffsetDateTime rescheduledAt;

    public AppointmentRescheduleHistoryEntity() {
    }

    public AppointmentRescheduleHistoryEntity(
            AppointmentEntity appointment,
            OffsetDateTime previousStartAt,
            OffsetDateTime previousEndAt,
            OffsetDateTime newStartAt,
            OffsetDateTime newEndAt) {

        this.appointment = appointment;
        this.previousStartAt = previousStartAt;
        this.previousEndAt = previousEndAt;
        this.newStartAt = newStartAt;
        this.newEndAt = newEndAt;
        this.rescheduledAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AppointmentEntity getAppointment() {
        return appointment;
    }

    public OffsetDateTime getPreviousStartAt() {
        return previousStartAt;
    }

    public OffsetDateTime getPreviousEndAt() {
        return previousEndAt;
    }

    public OffsetDateTime getNewStartAt() {
        return newStartAt;
    }

    public OffsetDateTime getNewEndAt() {
        return newEndAt;
    }

    public OffsetDateTime getRescheduledAt() {
        return rescheduledAt;
    }
}
