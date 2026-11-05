
package com.nexora.api.service;

import com.nexora.api.dto.CreateServiceRequest;
import com.nexora.api.entity.ServiceEntity;
import com.nexora.api.repository.ServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<ServiceEntity> getAllServices() {
        return serviceRepository.findAll();
    }

    public ServiceEntity getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Servicio no encontrado"
                ));
    }

    public ServiceEntity createService(CreateServiceRequest request) {

        ServiceEntity service = new ServiceEntity(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getDurationMinutes()
        );

        return serviceRepository.save(service);
    }

    public ServiceEntity updateService(Long id, CreateServiceRequest request) {

        ServiceEntity service = getServiceById(id);

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDurationMinutes(request.getDurationMinutes());

        return serviceRepository.save(service);
    }

    public ServiceEntity deactivateService(Long id) {

        ServiceEntity service = getServiceById(id);

        service.setActive(false);

        return serviceRepository.save(service);
    }
}
