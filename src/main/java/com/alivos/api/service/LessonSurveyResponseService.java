package com.alivos.api.service;

import com.alivos.api.dto.LessonSurveyResponseDto;
import com.alivos.api.dto.LessonSurveyResponseRequest;
import com.alivos.api.entity.LessonAttachment;
import com.alivos.api.entity.LessonAttachmentType;
import com.alivos.api.entity.LessonSurveyResponse;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.LessonAttachmentRepository;
import com.alivos.api.repository.LessonSurveyResponseRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonSurveyResponseService {

    private final LessonSurveyResponseRepository responseRepository;
    private final LessonAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public LessonSurveyResponseDto getMyResponse(String userId, String attachmentId) {
        return responseRepository.findByUserIdAndAttachmentId(userId, attachmentId)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public LessonSurveyResponseDto submitResponse(String userId, String attachmentId, LessonSurveyResponseRequest input) {
        LessonAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> ApiException.notFound("Encuesta no encontrada"));
        if (attachment.getType() != LessonAttachmentType.SURVEY) {
            throw ApiException.badRequest("Este adjunto no es una encuesta");
        }

        LessonSurveyResponse response = responseRepository.findByUserIdAndAttachmentId(userId, attachmentId)
                .orElseGet(() -> {
                    LessonSurveyResponse created = new LessonSurveyResponse();
                    created.setUser(userRepository.getReferenceById(userId));
                    created.setAttachment(attachment);
                    return created;
                });
        response.setAnswers(input.getAnswers());
        response = responseRepository.save(response);
        return toDto(response);
    }

    @Transactional(readOnly = true)
    public List<LessonSurveyResponseDto> listResponses(String attachmentId) {
        return responseRepository.findByAttachmentIdOrderByUpdatedAtDesc(attachmentId).stream()
                .map(this::toDto)
                .toList();
    }

    private LessonSurveyResponseDto toDto(LessonSurveyResponse response) {
        return new LessonSurveyResponseDto(
                response.getId(),
                response.getUser().getName(),
                response.getUser().getEmail(),
                response.getAnswers(),
                response.getUpdatedAt()
        );
    }
}
