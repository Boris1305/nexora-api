
package com.nexora.api.controller;

import com.nexora.api.dto.AppointmentResponse;
import com.nexora.api.dto.AppointmentRescheduleHistoryResponse;
import com.nexora.api.service.AppointmentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    private static final OffsetDateTime START_AT =
            OffsetDateTime.parse("2026-12-01T10:00:00-03:00");

    private static final OffsetDateTime END_AT =
            OffsetDateTime.parse("2026-12-01T11:00:00-03:00");

    private static final OffsetDateTime CREATED_AT =
            OffsetDateTime.parse("2026-11-12T10:00:00-03:00");

    private AppointmentResponse createResponse(
            String status,
            OffsetDateTime cancelledAt
    ) {
        AppointmentResponse.ClientDetails client =
                new AppointmentResponse.ClientDetails(
                        "Ana",
                        "Gomez",
                        "ana@example.com",
                        "1111111111",
                        "Cliente de prueba",
                        true,
                        CREATED_AT,
                        1L
                );

        AppointmentResponse.ServiceDetails service =
                new AppointmentResponse.ServiceDetails(
                        "Consulta inicial",
                        "Primera consulta profesional",
                        new BigDecimal("50000.00"),
                        60,
                        true,
                        CREATED_AT,
                        1L
                );

        return new AppointmentResponse(
                client,
                service,
                START_AT,
                END_AT,
                60,
                new BigDecimal("50000.00"),
                CREATED_AT,
                1L,
                status,
                cancelledAt
        );
    }

    @Test
    void shouldReturnAllAppointments() throws Exception {
        AppointmentResponse appointment =
                createResponse("SCHEDULED", null);

        when(appointmentService.getAllAppointments())
                .thenReturn(List.of(appointment));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[0].client.firstName").value("Ana"))
                .andExpect(jsonPath("$[0].service.name")
                        .value("Consulta inicial"));
    }

    @Test
    void shouldReturnAppointmentById() throws Exception {
        AppointmentResponse appointment =
                createResponse("SCHEDULED", null);

        when(appointmentService.getAppointmentById(1L))
                .thenReturn(appointment);

        mockMvc.perform(get("/api/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.durationMinutes").value(60));
    }

    @Test
    void shouldReturnRescheduleHistory() throws Exception {
        AppointmentRescheduleHistoryResponse history =
                new AppointmentRescheduleHistoryResponse(
                        1L,
                        1L,
                        START_AT,
                        END_AT,
                        START_AT.plusDays(1),
                        END_AT.plusDays(1),
                        CREATED_AT
                );

        when(appointmentService.getRescheduleHistory(1L))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/api/appointments/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].appointmentId").value(1))
                .andExpect(jsonPath("$[0].previousStartAt").exists())
                .andExpect(jsonPath("$[0].newStartAt").exists());
    }

    @Test
    void shouldCreateAppointment() throws Exception {
        AppointmentResponse appointment =
                createResponse("SCHEDULED", null);

        when(appointmentService.createAppointment(any()))
                .thenReturn(appointment);

        String requestBody = """
                {
                    "clientId": 1,
                    "serviceId": 1,
                    "startAt": "2026-12-01T10:00:00-03:00"
                }
                """;

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService).createAppointment(any());
    }

    @Test
    void shouldRejectAppointmentWithInvalidData() throws Exception {
        String requestBody = """
                {
                    "clientId": null,
                    "serviceId": 1,
                    "startAt": null
                }
                """;

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any());
    }

    @Test
    void shouldCancelAppointment() throws Exception {
        OffsetDateTime cancelledAt = CREATED_AT.plusMinutes(10);

        AppointmentResponse appointment =
                createResponse("CANCELLED", cancelledAt);

        when(appointmentService.cancelAppointment(1L))
                .thenReturn(appointment);

        mockMvc.perform(patch("/api/appointments/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists());
    }

    @Test
    void shouldRescheduleAppointment() throws Exception {
        AppointmentResponse appointment =
                createResponse("SCHEDULED", null);

        when(appointmentService.rescheduleAppointment(eq(1L), any()))
                .thenReturn(appointment);

        String requestBody = """
                {
                    "newStartAt": "2026-12-02T10:00:00-03:00"
                }
                """;

        mockMvc.perform(patch("/api/appointments/1/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(appointmentService).rescheduleAppointment(eq(1L), any());
    }

    @Test
    void shouldRejectRescheduleWithInvalidData() throws Exception {
        String requestBody = """
                {
                    "newStartAt": null
                }
                """;

        mockMvc.perform(patch("/api/appointments/1/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never())
                .rescheduleAppointment(eq(1L), any());
    }
}
