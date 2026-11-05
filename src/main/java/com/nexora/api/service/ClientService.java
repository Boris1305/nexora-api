
package com.nexora.api.service;

import com.nexora.api.dto.CreateClientRequest;
import com.nexora.api.entity.ClientEntity;
import com.nexora.api.repository.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public ClientEntity getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"
                ));
    }

    public ClientEntity createClient(CreateClientRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (clientRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un cliente registrado con ese email"
            );
        }

        ClientEntity client = new ClientEntity(
                request.getFirstName().trim(),
                request.getLastName().trim(),
                email,
                request.getPhone(),
                request.getNotes()
        );

        return clientRepository.save(client);
    }

    public ClientEntity updateClient(Long id, CreateClientRequest request) {

        ClientEntity client = getClientById(id);

        String email = request.getEmail().trim().toLowerCase();

        Optional<ClientEntity> existingClient =
                clientRepository.findByEmail(email);

        if (existingClient.isPresent()
                && !existingClient.get().getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro cliente registrado con ese email"
            );
        }

        client.setFirstName(request.getFirstName().trim());
        client.setLastName(request.getLastName().trim());
        client.setEmail(email);
        client.setPhone(request.getPhone());
        client.setNotes(request.getNotes());

        return clientRepository.save(client);
    }

    public ClientEntity deactivateClient(Long id) {

        ClientEntity client = getClientById(id);

        client.setActive(false);

        return clientRepository.save(client);
    }
}
