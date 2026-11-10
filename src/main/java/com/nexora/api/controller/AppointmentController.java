
package com.nexora.api.controller;

import com.nexora.api.dto.AppointmentResponse;
import com.nexora.api.dto.CreateAppointmentRequest;
import com.nexora.api.dto.RescheduleAppointmentRequest;
import com.nexora.api.dto.AppointmentRescheduleHistoryResponse;
import com.nexora.api.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @GetMapping("/{id}/history")
    public List<AppointmentRescheduleHistoryResponse> getRescheduleHistory(
            @PathVariable Long id) {
        return appointmentService.getRescheduleHistory(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.createAppointment(request);
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponse cancelAppointment(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }

    @PatchMapping("/{id}/reschedule")
    public AppointmentResponse rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return appointmentService.rescheduleAppointment(id, request);
    }
}
