
package com.nexora.api.controller;

import com.nexora.api.dto.CreateClientRequest;
import com.nexora.api.entity.ClientEntity;
import com.nexora.api.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Clientes",
        description = "Gestión de clientes de profesionales y pequeños negocios"
)
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(
            summary = "Listar clientes",
            description = "Obtiene la lista de clientes registrados."
    )
    public List<ClientEntity> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener cliente",
            description = "Busca un cliente por su identificador."
    )
    public ClientEntity getClientById(
            @Parameter(description = "Identificador del cliente", example = "1")
            @PathVariable Long id) {
        return clientService.getClientById(id);
    }

    @PostMapping
    @Operation(
            summary = "Crear cliente",
            description = "Registra un nuevo cliente. Los datos se validan antes de guardarse."
    )
    public ClientEntity createClient(
            @Valid @RequestBody CreateClientRequest request) {
        return clientService.createClient(request);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar cliente",
            description = "Actualiza los datos de un cliente existente."
    )
    public ClientEntity updateClient(
            @Parameter(description = "Identificador del cliente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CreateClientRequest request) {
        return clientService.updateClient(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Desactivar cliente",
            description = "Desactiva un cliente sin eliminar su registro."
    )
    public ClientEntity deactivateClient(
            @Parameter(description = "Identificador del cliente", example = "1")
            @PathVariable Long id) {
        return clientService.deactivateClient(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Reactivar cliente",
            description = "Vuelve a activar un cliente previamente desactivado."
    )
    public ClientEntity activateClient(
            @Parameter(description = "Identificador del cliente", example = "1")
            @PathVariable Long id) {
        return clientService.activateClient(id);
    }
}
