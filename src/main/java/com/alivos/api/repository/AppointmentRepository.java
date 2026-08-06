package com.alivos.api.repository;

import com.alivos.api.entity.Appointment;
import com.alivos.api.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {
    List<Appointment> findAllByOrderByDateDescTimeDesc();
    List<Appointment> findByUserIdOrderByDateDescTimeDesc(String userId);
    List<Appointment> findByDateAndStatusIn(LocalDate date, List<AppointmentStatus> statuses);
    List<Appointment> findByProfessionalIdAndDateAndStatusIn(String professionalId, LocalDate date, List<AppointmentStatus> statuses);
}
