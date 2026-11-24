
package com.nexora.api.service;

import com.nexora.api.dto.CreateClientRequest;
import com.nexora.api.entity.ClientEntity;
import com.nexora.api.repository.ClientRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void shouldReturnAllClients() {
        ClientEntity firstClient = new ClientEntity(
                "Ana", "Gomez", "ana@example.com", "1111111111", null
        );

        ClientEntity secondClient = new ClientEntity(
                "Luis", "Perez", "luis@example.com", "2222222222", null
        );

        List<ClientEntity> clients = List.of(firstClient, secondClient);

        when(clientRepository.findAll()).thenReturn(clients);

        List<ClientEntity> result = clientService.getAllClients();

        assertEquals(2, result.size());
        assertSame(firstClient, result.get(0));
        assertSame(secondClient, result.get(1));

        verify(clientRepository).findAll();
    }

    @Test
    void shouldReturnClientWhenIdExists() {
        Long clientId = 1L;

        ClientEntity client = new ClientEntity(
                "Ana", "Gomez", "ana@example.com", "1111111111", null
        );

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        ClientEntity result = clientService.getClientById(clientId);

        assertSame(client, result);
        verify(clientRepository).findById(clientId);
    }

    @Test
    void shouldThrowNotFoundWhenClientDoesNotExist() {
        Long clientId = 999L;

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clientService.getClientById(clientId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldCreateClientAndNormalizeNameAndEmail() {
        CreateClientRequest request = createRequest(
                "  Ana  ",
                "  Gomez  ",
                "  ANA@EXAMPLE.COM  ",
                "1111111111",
                "Cliente nuevo"
        );

        when(clientRepository.existsByEmail("ana@example.com"))
                .thenReturn(false);

        when(clientRepository.save(any(ClientEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClientEntity result = clientService.createClient(request);

        assertEquals("Ana", result.getFirstName());
        assertEquals("Gomez", result.getLastName());
        assertEquals("ana@example.com", result.getEmail());
        assertEquals("1111111111", result.getPhone());
        assertEquals("Cliente nuevo", result.getNotes());
        assertEquals(true, result.getActive());

        verify(clientRepository).existsByEmail("ana@example.com");
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void shouldRejectClientCreationWhenEmailAlreadyExists() {
        CreateClientRequest request = createRequest(
                "Ana",
                "Gomez",
                "ANA@EXAMPLE.COM",
                "1111111111",
                null
        );

        when(clientRepository.existsByEmail("ana@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clientService.createClient(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(clientRepository, never()).save(any(ClientEntity.class));
    }

    @Test
    void shouldUpdateClientWhenEmailBelongsToSameClient() {
        Long clientId = 1L;

        ClientEntity client = org.mockito.Mockito.mock(ClientEntity.class);

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        when(client.getId()).thenReturn(clientId);

        when(clientRepository.findByEmail("ana@example.com"))
                .thenReturn(Optional.of(client));

        when(clientRepository.save(client)).thenReturn(client);

        CreateClientRequest request = createRequest(
                "  Ana Maria  ",
                "  Gomez  ",
                "  ANA@EXAMPLE.COM  ",
                "3333333333",
                "Datos actualizados"
        );

        ClientEntity result = clientService.updateClient(clientId, request);

        assertSame(client, result);

        verify(client).setFirstName("Ana Maria");
        verify(client).setLastName("Gomez");
        verify(client).setEmail("ana@example.com");
        verify(client).setPhone("3333333333");
        verify(client).setNotes("Datos actualizados");

        verify(clientRepository).save(client);
    }

    @Test
    void shouldRejectUpdateWhenEmailBelongsToAnotherClient() {
        Long clientId = 1L;

        ClientEntity client = org.mockito.Mockito.mock(ClientEntity.class);
        ClientEntity anotherClient = org.mockito.Mockito.mock(ClientEntity.class);

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        when(clientRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(anotherClient));

        when(anotherClient.getId()).thenReturn(2L);

        CreateClientRequest request = createRequest(
                "Ana",
                "Gomez",
                "other@example.com",
                "3333333333",
                null
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clientService.updateClient(clientId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(clientRepository, never()).save(any(ClientEntity.class));
    }

    @Test
    void shouldRejectUpdateWhenClientDoesNotExist() {
        Long clientId = 999L;

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.empty());

        CreateClientRequest request = createRequest(
                "Ana",
                "Gomez",
                "ana@example.com",
                "1111111111",
                null
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clientService.updateClient(clientId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(clientRepository, never()).findByEmail(any(String.class));
        verify(clientRepository, never()).save(any(ClientEntity.class));
    }

    @Test
    void shouldDeactivateClient() {
        Long clientId = 1L;

        ClientEntity client = org.mockito.Mockito.mock(ClientEntity.class);

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        when(clientRepository.save(client)).thenReturn(client);

        ClientEntity result = clientService.deactivateClient(clientId);

        assertSame(client, result);

        verify(client).setActive(false);
        verify(clientRepository).save(client);
    }

    @Test
    void shouldActivateClient() {
        Long clientId = 1L;

        ClientEntity client = org.mockito.Mockito.mock(ClientEntity.class);

        when(clientRepository.findById(clientId))
                .thenReturn(Optional.of(client));

        when(clientRepository.save(client)).thenReturn(client);

        ClientEntity result = clientService.activateClient(clientId);

        assertSame(client, result);

        verify(client).setActive(true);
        verify(clientRepository).save(client);
    }

    private CreateClientRequest createRequest(
            String firstName,
            String lastName,
            String email,
            String phone,
            String notes) {

        CreateClientRequest request = new CreateClientRequest();

        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPhone(phone);
        request.setNotes(notes);

        return request;
    }
}
