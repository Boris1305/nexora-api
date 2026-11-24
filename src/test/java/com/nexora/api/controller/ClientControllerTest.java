
package com.nexora.api.controller;

import com.nexora.api.entity.ClientEntity;
import com.nexora.api.service.ClientService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @Test
    void shouldReturnAllClients() throws Exception {
        ClientEntity client = new ClientEntity(
                "Ana", "Gomez", "ana@example.com",
                "1111111111", "Cliente de prueba"
        );

        when(clientService.getAllClients()).thenReturn(List.of(client));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ana"))
                .andExpect(jsonPath("$[0].lastName").value("Gomez"))
                .andExpect(jsonPath("$[0].email").value("ana@example.com"));
    }

    @Test
    void shouldReturnClientById() throws Exception {
        ClientEntity client = new ClientEntity(
                "Ana", "Gomez", "ana@example.com",
                "1111111111", null
        );

        when(clientService.getClientById(1L)).thenReturn(client);

        mockMvc.perform(get("/api/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void shouldCreateClient() throws Exception {
        ClientEntity client = new ClientEntity(
                "Ana", "Gomez", "ana@example.com",
                "1111111111", null
        );

        when(clientService.createClient(any())).thenReturn(client);

        String requestBody = """
                {
                    "firstName": "Ana",
                    "lastName": "Gomez",
                    "email": "ana@example.com",
                    "phone": "1111111111",
                    "notes": "Cliente de prueba"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));

        verify(clientService).createClient(any());
    }

    @Test
    void shouldRejectClientWithInvalidData() throws Exception {
        String requestBody = """
                {
                    "firstName": "",
                    "lastName": "Gomez",
                    "email": "correo-invalido",
                    "phone": "1111111111"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any());
    }

    @Test
    void shouldUpdateClient() throws Exception {
        ClientEntity client = new ClientEntity(
                "Ana Maria", "Gomez", "ana@example.com",
                "2222222222", "Datos actualizados"
        );

        when(clientService.updateClient(
                org.mockito.ArgumentMatchers.eq(1L), any()
        )).thenReturn(client);

        String requestBody = """
                {
                    "firstName": "Ana Maria",
                    "lastName": "Gomez",
                    "email": "ana@example.com",
                    "phone": "2222222222",
                    "notes": "Datos actualizados"
                }
                """;

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Maria"))
                .andExpect(jsonPath("$.phone").value("2222222222"));
    }

    @Test
    void shouldDeactivateClient() throws Exception {
        ClientEntity client = new ClientEntity(
                "Ana", "Gomez", "ana@example.com",
                "1111111111", null
        );

        client.setActive(false);
        when(clientService.deactivateClient(1L)).thenReturn(client);

        mockMvc.perform(patch("/api/clients/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void shouldActivateClient() throws Exception {
        ClientEntity client = new ClientEntity(
                "Ana", "Gomez", "ana@example.com",
                "1111111111", null
        );

        when(clientService.activateClient(1L)).thenReturn(client);

        mockMvc.perform(patch("/api/clients/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}
