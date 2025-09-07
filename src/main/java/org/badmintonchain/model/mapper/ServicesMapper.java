package org.badmintonchain.model.mapper;

import org.badmintonchain.model.dto.ServiceDTO;
import org.badmintonchain.model.entity.ServicesEntity;

public class ServicesMapper {
    public static ServiceDTO toDTO(ServicesEntity entity) {
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setServiceId(entity.getId());
        serviceDTO.setServiceName(entity.getServiceName());
        serviceDTO.setServiceType(entity.getServiceType());
        serviceDTO.setDescription(entity.getDescription());
        serviceDTO.setUnitPrice(entity.getUnitPrice());
        serviceDTO.setCreatedAt(entity.getCreateAt());
        serviceDTO.setUpdatedAt( entity.getUpdateAt());
        return serviceDTO;
    }

    public static ServicesEntity toEntity(ServiceDTO dto) {
        ServicesEntity service = new ServicesEntity();
        service.setId(dto.getServiceId());
        service.setServiceName(dto.getServiceName());
        service.setServiceType(dto.getServiceType());
        service.setDescription(dto.getDescription());
        service.setUnitPrice(dto.getUnitPrice());
        service.setCreateAt(dto.getCreatedAt());
        service.setUpdateAt(dto.getUpdatedAt());
        return service;
    }
}
