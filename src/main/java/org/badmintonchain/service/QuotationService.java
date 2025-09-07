package org.badmintonchain.service;

import org.badmintonchain.model.dto.requests.QuotationRequest;

public interface QuotationService {
    void sendQuatation(QuotationRequest quotationRequest);
}
