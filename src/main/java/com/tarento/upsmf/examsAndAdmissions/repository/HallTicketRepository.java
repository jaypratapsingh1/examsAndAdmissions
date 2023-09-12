package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.HallTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface HallTicketRepository extends JpaRepository<HallTicket, Long> {
    Optional<HallTicket> findByExamRegistrationNumberAndDateOfBirth(String examRegistrationNumber, Date dateOfBirth);
}
