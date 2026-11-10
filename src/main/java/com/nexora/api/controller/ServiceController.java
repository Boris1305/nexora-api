
package com.nexora.api.controller;

import com.nexora.api.dto.CreateServiceRequest;
import com.nexora.api.entity.ServiceEntity;
import com.nexora.api.service.ServiceService;
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
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<ServiceEntity> getAllServices() {
        return serviceService.getAllServices();
    }

    @GetMapping("/{id}")
    public ServiceEntity getServiceById(@PathVariable Long id) {
        return serviceService.getServiceById(id);
    }

    @PostMapping
    public ServiceEntity createService(
            @Valid @RequestBody CreateServiceRequest request) {
        return serviceService.createService(request);
    }

    @PutMapping("/{id}")
    public ServiceEntity updateService(
            @PathVariable Long id,
            @Valid @RequestBody CreateServiceRequest request) {
        return serviceService.updateService(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public ServiceEntity deactivateService(@PathVariable Long id) {
        return serviceService.deactivateService(id);
    }

    @PatchMapping("/{id}/activate")
    public ServiceEntity activateService(@PathVariable Long id) {
        return serviceService.activateService(id);
    }
}
