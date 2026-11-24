
package com.nexora.api.controller;

import com.nexora.api.dto.AppointmentResponse;
import com.nexora.api.dto.CreateAppointmentRequest;
import com.nexora.api.dto.RescheduleAppointmentRequest;
import com.nexora.api.dto.AppointmentRescheduleHistoryResponse;
import com.nexora.api.service.AppointmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@Tag(
        name = "Turnos",
        description = "Gestión de turnos, cancelaciones, reprogramaciones e historial"
)
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @Operation(
            summary = "Listar turnos",
            description = "Obtiene la lista de turnos registrados."
    )
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener turno",
            description = "Busca un turno por su identificador."
    )
    public AppointmentResponse getAppointmentById(
            @Parameter(description = "Identificador del turno", example = "1")
            @PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @GetMapping("/{id}/history")
    @Operation(
            summary = "Consultar historial de reprogramaciones",
            description = "Obtiene el historial de cambios de fecha y horario de un turno."
    )
    public List<AppointmentRescheduleHistoryResponse> getRescheduleHistory(
            @Parameter(description = "Identificador del turno", example = "1")
            @PathVariable Long id) {
        return appointmentService.getRescheduleHistory(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear turno",
            description = "Registra un nuevo turno validando los datos y las reglas de negocio."
    )
    public AppointmentResponse createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        return appointmentService.createAppointment(request);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancelar turno",
            description = "Cancela un turno existente."
    )
    public AppointmentResponse cancelAppointment(
            @Parameter(description = "Identificador del turno", example = "1")
            @PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }

    @PatchMapping("/{id}/reschedule")
    @Operation(
            summary = "Reprogramar turno",
            description = "Modifica la fecha u horario de un turno y conserva la trazabilidad del cambio."
    )
    public AppointmentResponse rescheduleAppointment(
            @Parameter(description = "Identificador del turno", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return appointmentService.rescheduleAppointment(id, request);
    }
}
