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
        
        // Get all student enrollments for this curricular unit (regardless of status)
        // This ensures that students still appear in evaluation details even after their status changes
        List<StudentEnrollment> enrollments = studentEnrollmentRepository
                .findByCurricularUnitId(evaluation.getCurricularUnit().getId());
        
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

    @Transactional(readOnly = true)
    public List<EvaluationGradeDto> getAllRevisionRequests() {
        return evaluationGradeRepository.findAllGradedEvaluations().stream()
                .map(EvaluationGradeDto::new)
                .toList();
    }

    @Transactional
    public EvaluationGradeDto submitTeacherRevision(Long gradeId, Double suggestedGrade, String justification) {
        EvaluationGrade grade = fetchEvaluationGradeOrThrow(gradeId);
        grade.submitTeacherRevision(suggestedGrade, justification);
        return new EvaluationGradeDto(evaluationGradeRepository.save(grade));
    }

    @Transactional
    public EvaluationGradeDto approveFinalRevision(Long gradeId, String finalJustification, Boolean approved, Double finalGrade) {
        EvaluationGrade grade = fetchEvaluationGradeOrThrow(gradeId);
        
        if (approved && finalGrade != null) {
            // Approve the revision and update the grade
            grade.approveFinalRevision(finalJustification, finalGrade);
        } else {
            // Reject the revision and send back to teacher review
            grade.rejectFinalRevision(finalJustification);
        }
        
        EvaluationGrade savedGrade = evaluationGradeRepository.save(grade);
        
        // Check and update student completion if grade was approved
        if (approved && finalGrade != null) {
            StudentEnrollment studentEnrollment = savedGrade.getStudentEnrollment();
            checkAndUpdateStudentCompletion(studentEnrollment);
        }
        
        return new EvaluationGradeDto(savedGrade);
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
        if ( totalWeight == 1 && Math.abs(gradedWeight - totalWeight) < 0.001 && totalWeight > 0.0) {
            // Calculate final grade as weighted average
            double finalGrade = weightedSum / totalWeight;
            
            // Update student enrollment with final grade and completion status
            studentEnrollment.complete(finalGrade);
            studentEnrollmentRepository.save(studentEnrollment);
            
            // Send email notification to student about final grade
            try {
                String studentEmail = studentEnrollment.getStudent().getEmail();
                String subject = "Atribuição de Nota Final - " + studentEnrollment.getCurricularUnit().getName();
                String body = String.format("Caro(a) %s,\n\nA sua nota final para %s foi atribuída: %.0f valores.\nA mesma pode ser consultada ou revista mediante pedido, no painel 'Minhas UCs' do Sistema AMS.\n\nAtenciosamente,\n\nSistema AMS ",
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
    
    // Helper to send email to MailCrab (localhost:1025) with DEI logo signature
    private void sendFinalGradeEmail(String to, String subject, String body) {
        try {
            java.util.Properties props = new java.util.Properties();
            props.put("mail.smtp.host", "localhost");
            props.put("mail.smtp.port", "1025");
            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, null);
            
            jakarta.mail.internet.MimeMessage msg = new jakarta.mail.internet.MimeMessage(session);
            msg.setFrom(new jakarta.mail.internet.InternetAddress("ams@localhost"));
            msg.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(to, false));
            msg.setSubject(subject);
            
            // Create HTML email with signature image
            String htmlBody = createHtmlEmailBody(body);
            msg.setContent(htmlBody, "text/html; charset=utf-8");
            
            msg.setHeader("X-Mailer", "AMS System");
            msg.setSentDate(new java.util.Date());
            jakarta.mail.Transport.send(msg);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send email: " + e.getMessage());
        }
    }
    
    private String createHtmlEmailBody(String textBody) {
        // Convert plain text to HTML (preserve line breaks)
        String htmlText = textBody.replace("\n", "<br>");
        
        // DEI logo (base64)
        String deiLogoBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAABWwAAAHuCAYAAAALPkXoAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYwIDYxLjEzNDc3NywgMjAxMC8wMi8xMi0xNzozMjowMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNSBNYWNpbnRvc2giIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MTkzNjM2RDBGRTdBMTFFMUI5MEVDNTBFMjI5MThFMTIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MTkzNjM2RDFGRTdBMTFFMUI5MEVDNTBFMjI5MThFMTIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDoxOTM2MzZDRUZFN0ExMUUxQjkwRUM1MEUyMjkxOEUxMiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDoxOTM2MzZDRkZFN0ExMUUxQjkwRUM1MEUyMjkxOEUxMiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PnRZi60AAFU9SURBVHja7N1Nb9xY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";
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .email-content { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .signature { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; }
                    .logo { max-width: 150px; height: auto; }
                </style>
            </head>
            <body>
                <div class="email-content">
                    <div>%s</div>
                    <div class="signature">
                        <img src="%s" alt="DEI Logo" class="logo">
                        <p><strong>Sistema de Gestão Académica (AMS)</strong><br>
                        Departamento de Engenharia Informática<br>
                        Instituto Superior Técnico</p>
                    </div>
                </div>
            </body>
            </html>
            """, htmlText, deiLogoBase64);
    }
}
