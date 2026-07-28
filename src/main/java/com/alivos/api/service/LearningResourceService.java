package com.alivos.api.service;

import com.alivos.api.dto.LearningResourceDto;
import com.alivos.api.dto.LearningResourceRequest;
import com.alivos.api.dto.VimeoResolvedDto;
import com.alivos.api.entity.LearningResource;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private final LearningResourceRepository learningResourceRepository;
    private final VimeoService vimeoService;

    @Transactional(readOnly = true)
    public List<LearningResourceDto> listPublished() {
        return learningResourceRepository.findByVisibleTrueOrderByOrderIndexDescCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LearningResourceDto> listAll() {
        return learningResourceRepository.findAllByOrderByOrderIndexDescCreatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public LearningResourceDto create(LearningResourceRequest input) {
        if (isBlank(input.getTitle())) {
            throw ApiException.badRequest("El título es obligatorio");
        }
        LearningResource resource = new LearningResource();
        resource.setTitle(input.getTitle());
        resource.setType(input.getType() != null ? input.getType() : com.alivos.api.entity.ResourceType.PDF);
        applyFields(resource, input);
        resource = learningResourceRepository.save(resource);
        return toDto(resource);
    }

    @Transactional
    public LearningResourceDto update(String id, LearningResourceRequest input) {
        LearningResource resource = learningResourceRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Recurso no encontrado"));
        if (input.getTitle() != null) resource.setTitle(input.getTitle());
        if (input.getType() != null) resource.setType(input.getType());
        applyFields(resource, input);
        resource = learningResourceRepository.save(resource);
        return toDto(resource);
    }

    @Transactional
    public void delete(String id) {
        if (!learningResourceRepository.existsById(id)) {
            throw ApiException.notFound("Recurso no encontrado");
        }
        learningResourceRepository.deleteById(id);
    }

    private void applyFields(LearningResource resource, LearningResourceRequest input) {
        if (input.getDescription() != null) resource.setDescription(input.getDescription());
        if (input.getCoverImage() != null) resource.setCoverImage(input.getCoverImage());
        if (input.getFileUrl() != null) resource.setFileUrl(input.getFileUrl());
        if (input.getContent() != null) resource.setContent(input.getContent());
        if (input.getExternalUrl() != null) resource.setExternalUrl(input.getExternalUrl());
        if (input.getVisible() != null) resource.setVisible(input.getVisible());
        if (input.getOrder() != null) resource.setOrderIndex(input.getOrder());
        if (input.getVimeoUrl() != null) {
            if (input.getVimeoUrl().isBlank()) {
                resource.setVimeoUrl(null);
                resource.setVimeoEmbedUrl(null);
                resource.setVimeoThumbnail(null);
            } else {
                VimeoResolvedDto resolved = vimeoService.resolve(input.getVimeoUrl());
                resource.setVimeoUrl(input.getVimeoUrl());
                resource.setVimeoEmbedUrl(resolved.getEmbedUrl());
                resource.setVimeoThumbnail(resolved.getThumbnailUrl());
            }
        }
    }

    private LearningResourceDto toDto(LearningResource r) {
        return new LearningResourceDto(
                r.getId(),
                r.getTitle(),
                r.getDescription(),
                r.getType(),
                r.getCoverImage(),
                r.getFileUrl(),
                r.getVimeoUrl(),
                r.getVimeoEmbedUrl(),
                r.getVimeoThumbnail(),
                r.getContent(),
                r.getExternalUrl(),
                r.getVisible(),
                r.getCreatedAt()
        );
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
