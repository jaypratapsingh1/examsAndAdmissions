package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.enums.ApprovalStatus;
import com.tarento.upsmf.examsAndAdmissions.model.ExamFee;
import com.tarento.upsmf.examsAndAdmissions.model.HallTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HallTicketRepository extends JpaRepository<HallTicket, Long> {
    Optional<HallTicket> findByExamRegistrationNumberAndDateOfBirth(String examRegistrationNumber, Date dateOfBirth);
    @Query("SELECT e, a FROM ExamFee e JOIN AttendanceRecord a ON e.examCycle.id = a.examCycle.id WHERE e.status = :status AND a.approvalStatus = :approvalStatus")
    List<Object[]> findByStatusAndAttendanceApproval(@Param("status") ExamFee.Status status, @Param("approvalStatus") ApprovalStatus approvalStatus);
}
