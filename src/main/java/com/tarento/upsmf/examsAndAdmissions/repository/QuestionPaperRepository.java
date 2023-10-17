package com.tarento.upsmf.examsAndAdmissions.repository;

import com.tarento.upsmf.examsAndAdmissions.model.QuestionPaper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionPaperRepository extends JpaRepository<QuestionPaper, Long> {
    List<QuestionPaper> findByExamCycleIdAndExamId(Long examCycleId, Long id);
    List<QuestionPaper> findByExamCycleIdAndExamIdAndObsolete(Long examCycleId, Long examId, int obsolete);
}
