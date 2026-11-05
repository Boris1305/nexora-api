
package com.nexora.api.controller;

import com.nexora.api.dto.CreateClientRequest;
import com.nexora.api.entity.ClientEntity;
import com.nexora.api.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientEntity> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    public ClientEntity getClientById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }

    @PostMapping
    public ClientEntity createClient(
            @Valid @RequestBody CreateClientRequest request) {

        return clientService.createClient(request);
    }

    @PutMapping("/{id}")
    public ClientEntity updateClient(
            @PathVariable Long id,
            @Valid @RequestBody CreateClientRequest request) {

        return clientService.updateClient(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public ClientEntity deactivateClient(@PathVariable Long id) {
        return clientService.deactivateClient(id);
    }
}
