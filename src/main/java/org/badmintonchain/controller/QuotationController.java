package org.badmintonchain.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.badmintonchain.model.dto.requests.QuotationRequest;
import org.badmintonchain.service.QuotationService;
import org.badmintonchain.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuotationController {
    @Autowired
    private QuotationService quotationService;

    @PostMapping("/quotations/send")
    public ResponseEntity<ApiResponse<String>> sendQuotation(@RequestBody QuotationRequest quotationRequest,
                                                             HttpServletRequest httpRequest){
        quotationService.sendQuatation(quotationRequest);
        return ResponseEntity.ok(new ApiResponse<>(
                "Quotation sent successfully",
                HttpStatus.OK.value(),
                null,
                httpRequest.getRequestURI()
                )
        );
    }
}
