package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.ServiceDTO;
import org.badmintonchain.service.ServicesService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/services")
public class ServiceController {
    @Autowired
    private ServicesService servicesService;


    @PostMapping
    public ResponseEntity<ApiResponse<ServiceDTO>> createService(@RequestBody ServiceDTO dto,
                                                                 HttpServletRequest request) {
        ServiceDTO created = servicesService.createService(dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Service created successfully",
                        HttpStatus.CREATED.value(),
                        created,
                        request.getRequestURI())
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDTO>>> getAllServices(HttpServletRequest request) {
        List<ServiceDTO> services = servicesService.getAllServices();
        return ResponseEntity.ok(
                new ApiResponse<>("Services retrieved successfully",
                        HttpStatus.OK.value(),
                        services,
                        request.getRequestURI())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> getServiceById( @PathVariable Long id,
                                                                   HttpServletRequest request) {
        ServiceDTO service = servicesService.getServiceById(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Service retrieved successfully",
                        HttpStatus.OK.value(),
                        service,
                        request.getRequestURI())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDTO>> updateService( @PathVariable Long id,
                                                                  @RequestBody ServiceDTO dto,
                                                                  HttpServletRequest request) {
        ServiceDTO updated = servicesService.updateService(id, dto);
        return ResponseEntity.ok(
                new ApiResponse<>("Service updated successfully",
                        HttpStatus.OK.value(),
                        updated,
                        request.getRequestURI())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteService(@PathVariable Long id,
                                                             HttpServletRequest request) {
        servicesService.deleteService(id);
        return ResponseEntity.ok(
                new ApiResponse<>("Service deleted successfully",
                        HttpStatus.NO_CONTENT.value(),
                        null,
                        request.getRequestURI())
        );
    }
}
