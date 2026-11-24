
package com.nexora.api.controller;

import com.nexora.api.entity.ServiceEntity;
import com.nexora.api.service.ServiceService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceController.class)
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceService serviceService;

    @Test
    void shouldReturnAllServices() throws Exception {
        ServiceEntity service = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta profesional",
                new BigDecimal("50000.00"),
                60
        );

        when(serviceService.getAllServices()).thenReturn(List.of(service));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Consulta inicial"))
                .andExpect(jsonPath("$[0].description")
                        .value("Primera consulta profesional"))
                .andExpect(jsonPath("$[0].durationMinutes").value(60));
    }

    @Test
    void shouldReturnServiceById() throws Exception {
        ServiceEntity service = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta profesional",
                new BigDecimal("50000.00"),
                60
        );

        when(serviceService.getServiceById(1L)).thenReturn(service);

        mockMvc.perform(get("/api/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.durationMinutes").value(60));
    }

    @Test
    void shouldCreateService() throws Exception {
        ServiceEntity service = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta profesional",
                new BigDecimal("50000.00"),
                60
        );

        when(serviceService.createService(any())).thenReturn(service);

        String requestBody = """
                {
                    "name": "Consulta inicial",
                    "description": "Primera consulta profesional",
                    "price": 50000.00,
                    "durationMinutes": 60
                }
                """;

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consulta inicial"))
                .andExpect(jsonPath("$.durationMinutes").value(60));

        verify(serviceService).createService(any());
    }

    @Test
    void shouldRejectServiceWithInvalidData() throws Exception {
        String requestBody = """
                {
                    "name": "",
                    "description": "Servicio de prueba",
                    "price": -5000,
                    "durationMinutes": 0
                }
                """;

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any());
    }

    @Test
    void shouldUpdateService() throws Exception {
        ServiceEntity service = new ServiceEntity(
                "Consulta de evaluación",
                "Primera consulta profesional",
                new BigDecimal("60000.00"),
                90
        );

        when(serviceService.updateService(eq(1L), any()))
                .thenReturn(service);

        String requestBody = """
                {
                    "name": "Consulta de evaluación",
                    "description": "Primera consulta profesional",
                    "price": 60000.00,
                    "durationMinutes": 90
                }
                """;

        mockMvc.perform(put("/api/services/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Consulta de evaluación"))
                .andExpect(jsonPath("$.durationMinutes").value(90));
    }

    @Test
    void shouldDeactivateService() throws Exception {
        ServiceEntity service = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta profesional",
                new BigDecimal("50000.00"),
                60
        );

        service.setActive(false);

        when(serviceService.deactivateService(1L)).thenReturn(service);

        mockMvc.perform(patch("/api/services/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldActivateService() throws Exception {
        ServiceEntity service = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta profesional",
                new BigDecimal("50000.00"),
                60
        );

        when(serviceService.activateService(1L)).thenReturn(service);

        mockMvc.perform(patch("/api/services/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}
