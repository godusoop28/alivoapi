package com.alivos.api.service;

import com.alivos.api.dto.ProfessionalDto;
import com.alivos.api.dto.ProfessionalRequest;
import com.alivos.api.entity.Professional;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.ProfessionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;

    @Transactional(readOnly = true)
    public List<ProfessionalDto> listActive() {
        return professionalRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProfessionalDto> listAll() {
        return professionalRepository.findAllByOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public ProfessionalDto create(ProfessionalRequest input) {
        if (isBlank(input.getName()) || isBlank(input.getTitle())) {
            throw ApiException.badRequest("El nombre y el título son obligatorios");
        }
        Professional professional = new Professional();
        applyInput(professional, input);
        professional = professionalRepository.save(professional);
        return toDto(professional);
    }

    @Transactional
    public ProfessionalDto update(String id, ProfessionalRequest input) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Profesional no encontrado"));
        applyInput(professional, input);
        professional = professionalRepository.save(professional);
        return toDto(professional);
    }

    @Transactional
    public void deactivate(String id) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Profesional no encontrado"));
        professional.setActive(false);
        professionalRepository.save(professional);
    }

    private void applyInput(Professional professional, ProfessionalRequest input) {
        if (input.getName() != null) professional.setName(input.getName());
        if (input.getTitle() != null) professional.setTitle(input.getTitle());
        if (input.getBio() != null) professional.setBio(input.getBio());
        if (input.getPhotoUrl() != null) professional.setPhotoUrl(input.getPhotoUrl());
        if (input.getActive() != null) professional.setActive(input.getActive());
        if (input.getSlotMinutes() != null) professional.setSlotMinutes(input.getSlotMinutes());
        if (input.getWorkDays() != null) professional.setWorkDays(input.getWorkDays());
        if (input.getStartTime() != null) professional.setStartTime(input.getStartTime());
        if (input.getEndTime() != null) professional.setEndTime(input.getEndTime());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ProfessionalDto toDto(Professional p) {
        return new ProfessionalDto(
                p.getId(), p.getName(), p.getTitle(), p.getBio(), p.getPhotoUrl(),
                p.getActive(), p.getSlotMinutes(), p.getWorkDays(), p.getStartTime(), p.getEndTime()
        );
    }
}
