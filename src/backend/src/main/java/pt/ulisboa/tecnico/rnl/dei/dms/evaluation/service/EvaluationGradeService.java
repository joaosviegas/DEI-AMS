package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Evaluation;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.EvaluationGrade;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Test;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Project;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.EvaluationGradeDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.EvaluationGradeRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.TestRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.ProjectRepository;
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
    private ProjectRepository projectRepository;

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    private EvaluationGrade fetchEvaluationGradeOrThrow(Long id) {
        return evaluationGradeRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_EVALUATION_GRADE, Long.toString(id)));
    }

    private Evaluation fetchEvaluationOrThrow(Long id) {
        // Try to find in test repository first
        return testRepository.findById(id)
                .map(test -> (Evaluation) test)
                .orElseGet(() -> projectRepository.findById(id)
                        .map(project -> (Evaluation) project)
                        .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_TEST, Long.toString(id))));
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
        
        EvaluationGradeDto result;
        // Check if grade already exists
        EvaluationGrade existingGrade = evaluationGradeRepository
                .findByEvaluationIdAndStudentEnrollmentId(evaluationId, studentEnrollmentId)
                .orElse(null);
        
        if (existingGrade != null) {
            // Update existing grade
            existingGrade.updateGrade(grade);
            result = new EvaluationGradeDto(evaluationGradeRepository.save(existingGrade));
        } else {
            // Create new grade
            EvaluationGrade newGrade = new EvaluationGrade(evaluation, studentEnrollment, grade);
            result = new EvaluationGradeDto(evaluationGradeRepository.save(newGrade));
        }
        
        // Check if student has completed all evaluations and update enrollment if necessary
        checkAndUpdateStudentCompletion(studentEnrollment);
        
        return result;
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

    /**
     * Checks if a student has completed all evaluations (100% weight) and updates their enrollment
     */
    @Transactional
    public void checkAndUpdateStudentCompletion(StudentEnrollment studentEnrollment) {
        if (studentEnrollment.isCompleted()) {
            // Already completed, don't update
            return;
        }

        Long curricularUnitId = studentEnrollment.getCurricularUnit().getId();
        Long studentEnrollmentId = studentEnrollment.getId();
        
        // Get all evaluations for this curricular unit
        List<Test> tests = testRepository.findByCurricularUnitId(curricularUnitId);
        List<Project> projects = projectRepository.findByCurricularUnitId(curricularUnitId);
        
        // Get all grades for this student in this curricular unit
        List<EvaluationGrade> grades = evaluationGradeRepository
                .findByStudentEnrollmentIdAndCurricularUnitId(studentEnrollmentId, curricularUnitId);
        
        // Calculate total weight of evaluations and graded evaluations
        double totalWeight = 0.0;
        double gradedWeight = 0.0;
        double weightedSum = 0.0;
        
        // Add test weights
        for (Test test : tests) {
            totalWeight += test.getWeight();
            
            // Check if this test is graded
            for (EvaluationGrade grade : grades) {
                if (grade.getEvaluation().getId().equals(test.getId())) {
                    gradedWeight += test.getWeight();
                    weightedSum += grade.getGrade() * test.getWeight();
                    break;
                }
            }
        }
        
        // Add project weights
        for (Project project : projects) {
            totalWeight += project.getWeight();
            
            // Check if this project is graded
            for (EvaluationGrade grade : grades) {
                if (grade.getEvaluation().getId().equals(project.getId())) {
                    gradedWeight += project.getWeight();
                    weightedSum += grade.getGrade() * project.getWeight();
                    break;
                }
            }
        }
        
        // Check if all evaluations are graded (allowing for small floating point errors)
        if (Math.abs(gradedWeight - totalWeight) < 0.001 && totalWeight > 0.0) {
            // Calculate final grade as weighted average
            double finalGrade = weightedSum / totalWeight;
            
            // Update student enrollment with final grade and completion status
            studentEnrollment.complete(finalGrade);
            studentEnrollmentRepository.save(studentEnrollment);
            
            // Send email notification to student about final grade
            try {
                String studentEmail = studentEnrollment.getStudent().getEmail();
                String subject = "Atribuição de Nota Final - " + studentEnrollment.getCurricularUnit().getName();
                String body = String.format("Caro(a) %s,\n\nA sua nota final para %s foi atribuída: %.0f.\n\nAtenciosamente,\nSistema AMS ",
                    studentEnrollment.getStudent().getName(),
                    studentEnrollment.getCurricularUnit().getName(),
                    finalGrade
                );
                sendFinalGradeEmail(studentEmail, subject, body);
            } catch (Exception e) {
                // Log but do not interrupt flow
                System.err.println("[WARN] Could not send final grade email: " + e.getMessage());
            }
        }
    }
    
    // Helper to send email to MailCrab (localhost:1025)
    private void sendFinalGradeEmail(String to, String subject, String body) {
        try {
            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.host", "localhost");
            props.put("mail.smtp.port", "1025");
            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, null);
            jakarta.mail.Message msg = new jakarta.mail.internet.MimeMessage(session);
            msg.setFrom(new jakarta.mail.internet.InternetAddress("ams@localhost"));
            msg.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setHeader("X-Mailer", "AMS System");
            msg.setSentDate(new java.util.Date());
            jakarta.mail.Transport.send(msg);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send email: " + e.getMessage());
        }
    }
}
