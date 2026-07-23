package com.alivos.api.service;

import com.alivos.api.dto.PurchaseDto;
import com.alivos.api.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public List<PurchaseDto> listPurchases() {
        return purchaseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> new PurchaseDto(
                        p.getId(), p.getUser().getName(), p.getUser().getEmail(), p.getCourse().getTitle(),
                        p.getAmount(), p.getStatus(), p.getMethod(), p.getPaymentId(), p.getCreatedAt()
                )).toList();
    }
}
