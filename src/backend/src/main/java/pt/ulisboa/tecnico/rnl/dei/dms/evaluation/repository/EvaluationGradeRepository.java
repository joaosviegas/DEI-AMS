package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.EvaluationGrade;

import java.util.List;
import java.util.Optional;

// Repository for EvaluationGrade entities
@Repository
public interface EvaluationGradeRepository extends JpaRepository<EvaluationGrade, Long> {

    // Find all grades for a specific evaluation
    List<EvaluationGrade> findByEvaluationId(Long evaluationId);

    // Find grade for a specific evaluation and student enrollment
    Optional<EvaluationGrade> findByEvaluationIdAndStudentEnrollmentId(Long evaluationId, Long studentEnrollmentId);

    // Find all grades for a specific student enrollment
    List<EvaluationGrade> findByStudentEnrollmentId(Long studentEnrollmentId);

    // Find all grades for a specific student enrollment in a specific curricular unit
    @Query("SELECT eg FROM EvaluationGrade eg WHERE eg.studentEnrollment.id = :studentEnrollmentId AND eg.evaluation.curricularUnit.id = :curricularUnitId")
    List<EvaluationGrade> findByStudentEnrollmentIdAndCurricularUnitId(@Param("studentEnrollmentId") Long studentEnrollmentId, @Param("curricularUnitId") Long curricularUnitId);

    // Find all grades with pending revision requests
    @Query("SELECT eg FROM EvaluationGrade eg WHERE eg.revisionRequested = true")
    List<EvaluationGrade> findPendingRevisions();

    // Find all grades with pending revision requests for a specific curricular unit
    @Query("SELECT eg FROM EvaluationGrade eg WHERE eg.revisionRequested = true AND eg.evaluation.curricularUnit.id = :curricularUnitId")
    List<EvaluationGrade> findPendingRevisionsByCurricularUnit(@Param("curricularUnitId") Long curricularUnitId);

    // Find all graded evaluations (evaluations that have a grade assigned)
    @Query("SELECT eg FROM EvaluationGrade eg " +
           "JOIN FETCH eg.evaluation e " +
           "JOIN FETCH e.curricularUnit cu " +
           "JOIN FETCH eg.studentEnrollment se " +
           "JOIN FETCH se.student s " +
           "WHERE eg.grade IS NOT NULL")
    List<EvaluationGrade> findAllGradedEvaluations();
}
