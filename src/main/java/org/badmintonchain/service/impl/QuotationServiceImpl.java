package org.badmintonchain.service.impl;

import org.badmintonchain.exceptions.ServicesException;
import org.badmintonchain.model.dto.requests.QuotationRequest;
import org.badmintonchain.model.entity.ServicesEntity;
import org.badmintonchain.repository.ServiceRepository;
import org.badmintonchain.service.EmailService;
import org.badmintonchain.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class QuotationServiceImpl implements QuotationService {

    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private EmailService emailService;

    @Override
    public void sendQuatation(QuotationRequest request) {
        ServicesEntity  service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ServicesException("Service not found"));
        BigDecimal totalPrice = service.getUnitPrice() .multiply(BigDecimal.valueOf(request.getQuantity()));

        String emailContent =  String.format(
                "Báo giá dịch vụ: %s\nĐơn giá: %s VND\nSố lượng: %d\nTổng cộng: %s VND\nLiên hệ: %s",
                service.getServiceName(),
                service.getUnitPrice(),
                request.getQuantity(),
                totalPrice,
                request.getCustomerPhone()
        );

        emailService.sendQuotationEmail(request.getCustomerEmail(),"Báo giá dịch vụ -", emailContent);

    }
}
