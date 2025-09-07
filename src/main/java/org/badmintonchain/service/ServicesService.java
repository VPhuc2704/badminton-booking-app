package org.badmintonchain.service;

import org.badmintonchain.model.dto.ServiceDTO;
import org.badmintonchain.model.entity.ServicesEntity;
import org.badmintonchain.model.mapper.ServicesMapper;

import java.time.LocalDateTime;
import java.util.List;

public interface ServicesService {
    ServiceDTO createService(ServiceDTO dto);
    List<ServiceDTO> getAllServices();
    ServiceDTO getServiceById(Long id);
    ServiceDTO updateService(Long id, ServiceDTO dto);
    void deleteService(Long id);

}
