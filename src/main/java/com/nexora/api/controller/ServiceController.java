
package com.nexora.api.controller;

import com.nexora.api.dto.CreateServiceRequest;
import com.nexora.api.entity.ServiceEntity;
import com.nexora.api.service.ServiceService;
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
@RequestMapping("/api/services")
@Tag(
        name = "Servicios",
        description = "Gestión del catálogo de servicios profesionales."
)
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    @Operation(
            summary = "Listar servicios",
            description = "Obtiene todos los servicios registrados, incluyendo su estado de actividad."
    )
    public List<ServiceEntity> getAllServices() {
        return serviceService.getAllServices();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar un servicio",
            description = "Obtiene los datos de un servicio específico mediante su identificador."
    )
    public ServiceEntity getServiceById(
            @Parameter(description = "Identificador único del servicio", example = "1")
            @PathVariable Long id) {
        return serviceService.getServiceById(id);
    }

    @PostMapping
    @Operation(
            summary = "Crear un servicio",
            description = """
                    Registra un nuevo servicio profesional.
                    Valida los datos recibidos, incluyendo nombre, descripción,
                    precio y duración.
                    """
    )
    public ServiceEntity createService(
            @Valid @RequestBody CreateServiceRequest request) {
        return serviceService.createService(request);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un servicio",
            description = "Actualiza los datos de un servicio existente identificado por su ID."
    )
    public ServiceEntity updateService(
            @Parameter(description = "Identificador único del servicio", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CreateServiceRequest request) {
        return serviceService.updateService(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Desactivar un servicio",
            description = "Desactiva un servicio existente sin eliminar su registro."
    )
    public ServiceEntity deactivateService(
            @Parameter(description = "Identificador único del servicio", example = "1")
            @PathVariable Long id) {
        return serviceService.deactivateService(id);
    }

    @PatchMapping("/{id}/activate")
    @Operation(
            summary = "Activar un servicio",
            description = "Vuelve a activar un servicio previamente desactivado."
    )
    public ServiceEntity activateService(
            @Parameter(description = "Identificador único del servicio", example = "1")
            @PathVariable Long id) {
        return serviceService.activateService(id);
    }
}
