package com.alivos.api.service;

import com.alivos.api.dto.FormResponseDto;
import com.alivos.api.dto.FormResponseRequest;
import com.alivos.api.entity.FormResponse;
import com.alivos.api.entity.Lesson;
import com.alivos.api.exception.ApiException;
import com.alivos.api.repository.FormResponseRepository;
import com.alivos.api.repository.LessonRepository;
import com.alivos.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FormResponseService {

    private final FormResponseRepository formResponseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FormResponseDto getMyResponse(String userId, String lessonId) {
        return formResponseRepository.findByUserIdAndLessonId(userId, lessonId)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public FormResponseDto submitResponse(String userId, String lessonId, FormResponseRequest input) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> ApiException.notFound("Lección no encontrada"));

        FormResponse response = formResponseRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    FormResponse created = new FormResponse();
                    created.setUser(userRepository.getReferenceById(userId));
                    created.setLesson(lesson);
                    return created;
                });
        response.setAnswers(input.getAnswers());
        response = formResponseRepository.save(response);
        return toDto(response);
    }

    @Transactional(readOnly = true)
    public List<FormResponseDto> listResponses(String lessonId) {
        return formResponseRepository.findByLessonIdOrderByUpdatedAtDesc(lessonId).stream()
                .map(this::toDto)
                .toList();
    }

    private FormResponseDto toDto(FormResponse response) {
        return new FormResponseDto(
                response.getId(),
                response.getUser().getName(),
                response.getUser().getEmail(),
                response.getAnswers(),
                response.getUpdatedAt()
        );
    }
}
