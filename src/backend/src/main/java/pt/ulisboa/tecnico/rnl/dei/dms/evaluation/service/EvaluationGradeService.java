package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Evaluation;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.EvaluationGrade;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.EvaluationGradeDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.EvaluationGradeRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.TestRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.repository.StudentEnrollmentRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

import java.util.List;
import java.util.ArrayList;

// Service class for managing EvaluationGrade entities
@Service
@Transactional
public class EvaluationGradeService {

    @Autowired
    private EvaluationGradeRepository evaluationGradeRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    private EvaluationGrade fetchEvaluationGradeOrThrow(Long id) {
        return evaluationGradeRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_EVALUATION_GRADE, Long.toString(id)));
    }

    private Evaluation fetchEvaluationOrThrow(Long id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_TEST, Long.toString(id)));
    }

    private StudentEnrollment fetchStudentEnrollmentOrThrow(Long id) {
        return studentEnrollmentRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_STUDENT_ENROLLMENT, Long.toString(id)));
    }

    @Transactional(readOnly = true)
    public List<EvaluationGradeDto> getGradesByEvaluation(Long evaluationId) {
        Evaluation evaluation = fetchEvaluationOrThrow(evaluationId);
        
        // Get all student enrollments for this curricular unit
        List<StudentEnrollment> enrollments = studentEnrollmentRepository
                .findByCurricularUnitIdAndStatus(evaluation.getCurricularUnit().getId(), 
                                               StudentEnrollment.EnrollmentStatus.ENROLLED);
        
        List<EvaluationGradeDto> result = new ArrayList<>();
        
        for (StudentEnrollment enrollment : enrollments) {
            // Check if grade exists for this student
            EvaluationGrade existingGrade = evaluationGradeRepository
                    .findByEvaluationIdAndStudentEnrollmentId(evaluationId, enrollment.getId())
                    .orElse(null);
            
            if (existingGrade != null) {
                result.add(new EvaluationGradeDto(existingGrade));
            } else {
                // Create a DTO for student without grade
                EvaluationGradeDto dto = new EvaluationGradeDto();
                dto.setEvaluationId(evaluationId);
                dto.setStudentEnrollmentId(enrollment.getId());
                dto.setStudent(new PersonDto(enrollment.getStudent()));
                dto.setGrade(null);
                dto.setRevisionRequested(false);
                result.add(dto);
            }
        }
        
        return result;
    }

    @Transactional
    public EvaluationGradeDto saveGrade(Long evaluationId, Long studentEnrollmentId, Double grade) {
        Evaluation evaluation = fetchEvaluationOrThrow(evaluationId);
        StudentEnrollment studentEnrollment = fetchStudentEnrollmentOrThrow(studentEnrollmentId);
        
        // Validate grade
        if (grade == null || grade < 0.0 || grade > 20.0) {
            throw new DEIException(ErrorMessage.GRADE_NOT_VALID);
        }
        
        // Check if grade already exists
        EvaluationGrade existingGrade = evaluationGradeRepository
                .findByEvaluationIdAndStudentEnrollmentId(evaluationId, studentEnrollmentId)
                .orElse(null);
        
        if (existingGrade != null) {
            // Update existing grade
            existingGrade.updateGrade(grade);
            return new EvaluationGradeDto(evaluationGradeRepository.save(existingGrade));
        } else {
            // Create new grade
            EvaluationGrade newGrade = new EvaluationGrade(evaluation, studentEnrollment, grade);
            return new EvaluationGradeDto(evaluationGradeRepository.save(newGrade));
        }
    }

    @Transactional
    public EvaluationGradeDto requestRevision(Long gradeId, String reason) {
        EvaluationGrade grade = fetchEvaluationGradeOrThrow(gradeId);
        grade.requestRevision(reason);
        return new EvaluationGradeDto(evaluationGradeRepository.save(grade));
    }

    @Transactional(readOnly = true)
    public List<EvaluationGradeDto> getGradesByStudentEnrollment(Long studentEnrollmentId) {
        return evaluationGradeRepository.findByStudentEnrollmentId(studentEnrollmentId).stream()
                .map(EvaluationGradeDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EvaluationGradeDto> getPendingRevisions() {
        return evaluationGradeRepository.findPendingRevisions().stream()
                .map(EvaluationGradeDto::new)
                .toList();
    }
}
