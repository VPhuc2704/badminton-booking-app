package org.badmintonchain.service.impl;

import org.badmintonchain.exceptions.ServicesException;
import org.badmintonchain.model.dto.ServiceDTO;
import org.badmintonchain.model.entity.ServicesEntity;
import org.badmintonchain.model.mapper.ServicesMapper;
import org.badmintonchain.repository.ServiceRepository;
import org.badmintonchain.service.ServicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicesServiceImpl implements ServicesService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public ServiceDTO createService(ServiceDTO dto) {
        ServicesEntity entity = ServicesMapper.toEntity(dto);
        entity.setCreateAt(LocalDateTime.now());
        entity.setUpdateAt(LocalDateTime.now());
        return ServicesMapper.toDTO(serviceRepository.save(entity));
    }

    // Read all
    @Override
    public List<ServiceDTO> getAllServices() {
        return serviceRepository.findAll()
                .stream()
                .map(ServicesMapper::toDTO)
                .toList();
    }

    // Read one
    @Override
    public ServiceDTO getServiceById(Long id) {
        ServicesEntity entity = serviceRepository.findById(id)
                .orElseThrow(() -> new ServicesException("Service not found"));
        return ServicesMapper.toDTO(entity);
    }

    // Update
    @Override
    public ServiceDTO updateService(Long id, ServiceDTO dto) {
        ServicesEntity existing = serviceRepository.findById(id)
                .orElseThrow(() -> new ServicesException("Service not found"));


        if (dto.getServiceName() != null) {
            existing.setServiceName(dto.getServiceName());
        }
        if (dto.getServiceType() != null) {
            existing.setServiceType(dto.getServiceType());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getUnitPrice() != null) {
            existing.setUnitPrice(dto.getUnitPrice());
        }

        existing.setUpdateAt(LocalDateTime.now());

        ServicesEntity saved = serviceRepository.save(existing);
        return ServicesMapper.toDTO(saved);
    }

    // Delete
    @Override
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}
