package pt.ulisboa.tecnico.rnl.dei.dms.evaluation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.EvaluationGradeDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service.EvaluationGradeService;

import java.util.List;

// REST controller for EvaluationGrade operations
@RestController
@RequestMapping("/evaluation-grades")
public class EvaluationGradeController {

    @Autowired
    private EvaluationGradeService evaluationGradeService;

    @GetMapping("/evaluation/{evaluationId}")
    public List<EvaluationGradeDto> getGradesByEvaluation(@PathVariable Long evaluationId) {
        return evaluationGradeService.getGradesByEvaluation(evaluationId);
    }

    @PostMapping
    public EvaluationGradeDto saveGrade(
            @RequestParam Long evaluationId,
            @RequestParam Long studentEnrollmentId,
            @RequestParam Double grade) {
        return evaluationGradeService.saveGrade(evaluationId, studentEnrollmentId, grade);
    }

    @PutMapping("/{gradeId}/request-revision")
    public EvaluationGradeDto requestRevision(
            @PathVariable Long gradeId,
            @RequestParam String reason) {
        return evaluationGradeService.requestRevision(gradeId, reason);
    }

    @GetMapping("/student-enrollment/{studentEnrollmentId}")
    public List<EvaluationGradeDto> getGradesByStudentEnrollment(@PathVariable Long studentEnrollmentId) {
        return evaluationGradeService.getGradesByStudentEnrollment(studentEnrollmentId);
    }

    @GetMapping("/pending-revisions")
    public List<EvaluationGradeDto> getPendingRevisions() {
        return evaluationGradeService.getPendingRevisions();
    }

    @GetMapping("/revision-requests")
    public List<EvaluationGradeDto> getAllRevisionRequests() {
        return evaluationGradeService.getAllRevisionRequests();
    }

    @PutMapping("/{gradeId}/teacher-revision")
    public EvaluationGradeDto submitTeacherRevision(
            @PathVariable Long gradeId,
            @RequestParam Double suggestedGrade,
            @RequestParam String justification) {
        return evaluationGradeService.submitTeacherRevision(gradeId, suggestedGrade, justification);
    }

    @PutMapping("/{gradeId}/final-approval")
    public EvaluationGradeDto approveFinalRevision(
            @PathVariable Long gradeId,
            @RequestParam String justification,
            @RequestParam Boolean approved,
            @RequestParam(required = false) Double finalGrade) {
        return evaluationGradeService.approveFinalRevision(gradeId, justification, approved, finalGrade);
    }
}
