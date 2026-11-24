
package com.nexora.api.service;

import com.nexora.api.dto.CreateServiceRequest;
import com.nexora.api.entity.ServiceEntity;
import com.nexora.api.repository.ServiceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceService serviceService;

    @Test
    void shouldReturnAllServices() {
        ServiceEntity firstService = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta",
                new BigDecimal("50000.00"),
                60
        );

        ServiceEntity secondService = new ServiceEntity(
                "Consulta de seguimiento",
                "Control posterior",
                new BigDecimal("30000.00"),
                30
        );

        List<ServiceEntity> services = List.of(firstService, secondService);

        when(serviceRepository.findAll()).thenReturn(services);

        List<ServiceEntity> result = serviceService.getAllServices();

        assertEquals(2, result.size());
        assertSame(firstService, result.get(0));
        assertSame(secondService, result.get(1));

        verify(serviceRepository).findAll();
    }

    @Test
    void shouldReturnServiceWhenIdExists() {
        Long serviceId = 1L;

        ServiceEntity service = new ServiceEntity(
                "Consulta inicial",
                "Primera consulta",
                new BigDecimal("50000.00"),
                60
        );

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        ServiceEntity result = serviceService.getServiceById(serviceId);

        assertSame(service, result);
        verify(serviceRepository).findById(serviceId);
    }

    @Test
    void shouldThrowNotFoundWhenServiceDoesNotExist() {
        Long serviceId = 999L;

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> serviceService.getServiceById(serviceId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldCreateService() {
        CreateServiceRequest request = createRequest(
                "Consulta inicial",
                "Primera consulta",
                new BigDecimal("50000.00"),
                60
        );

        when(serviceRepository.save(any(ServiceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceEntity result = serviceService.createService(request);

        assertEquals("Consulta inicial", result.getName());
        assertEquals("Primera consulta", result.getDescription());
        assertEquals(
                new BigDecimal("50000.00"),
                result.getPrice()
        );
        assertEquals(60, result.getDurationMinutes());
        assertEquals(true, result.getActive());

        verify(serviceRepository).save(any(ServiceEntity.class));
    }

    @Test
    void shouldUpdateService() {
        Long serviceId = 1L;

        ServiceEntity service = org.mockito.Mockito.mock(ServiceEntity.class);

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        when(serviceRepository.save(service)).thenReturn(service);

        CreateServiceRequest request = createRequest(
                "Consulta de evaluación",
                "Primera consulta profesional",
                new BigDecimal("60000.00"),
                90
        );

        ServiceEntity result = serviceService.updateService(serviceId, request);

        assertSame(service, result);

        verify(service).setName("Consulta de evaluación");
        verify(service).setDescription("Primera consulta profesional");
        verify(service).setPrice(new BigDecimal("60000.00"));
        verify(service).setDurationMinutes(90);

        verify(serviceRepository).save(service);
    }

    @Test
    void shouldNotUpdateWhenServiceDoesNotExist() {
        Long serviceId = 999L;

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.empty());

        CreateServiceRequest request = createRequest(
                "Consulta inicial",
                "Primera consulta",
                new BigDecimal("50000.00"),
                60
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> serviceService.updateService(serviceId, request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(serviceRepository, never()).save(any(ServiceEntity.class));
    }

    @Test
    void shouldDeactivateService() {
        Long serviceId = 1L;

        ServiceEntity service = org.mockito.Mockito.mock(ServiceEntity.class);

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity result = serviceService.deactivateService(serviceId);

        assertSame(service, result);

        verify(service).setActive(false);
        verify(serviceRepository).save(service);
    }

    @Test
    void shouldActivateService() {
        Long serviceId = 1L;

        ServiceEntity service = org.mockito.Mockito.mock(ServiceEntity.class);

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        when(serviceRepository.save(service)).thenReturn(service);

        ServiceEntity result = serviceService.activateService(serviceId);

        assertSame(service, result);

        verify(service).setActive(true);
        verify(serviceRepository).save(service);
    }

    private CreateServiceRequest createRequest(
            String name,
            String description,
            BigDecimal price,
            Integer durationMinutes) {

        CreateServiceRequest request = new CreateServiceRequest();

        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setDurationMinutes(durationMinutes);

        return request;
    }
}
