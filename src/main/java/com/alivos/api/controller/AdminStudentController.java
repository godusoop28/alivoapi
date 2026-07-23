package com.alivos.api.controller;

import com.alivos.api.dto.StudentDetailDto;
import com.alivos.api.dto.StudentDto;
import com.alivos.api.dto.StudentStatusRequest;
import com.alivos.api.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStudentController {

    private final StudentService studentService;

    @GetMapping
    public Map<String, List<StudentDto>> listStudents() {
        return Map.of("students", studentService.listStudents());
    }

    @GetMapping("/{id}")
    public Map<String, StudentDetailDto> getStudent(@PathVariable String id) {
        return Map.of("student", studentService.getStudent(id));
    }

    @PatchMapping("/{id}/status")
    public Map<String, Boolean> setStatus(@PathVariable String id, @Valid @RequestBody StudentStatusRequest request) {
        studentService.setStudentStatus(id, request.getStatus());
        return Map.of("ok", true);
    }
}
