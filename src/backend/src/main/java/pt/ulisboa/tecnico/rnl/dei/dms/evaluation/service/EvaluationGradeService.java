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
        EvaluationGrade savedGrade = evaluationGradeRepository.save(grade);
        
        // Phase 1: Send email notification to all teachers of the UC after student requests revision
        try {
            sendRevisionRequestEmailToTeachers(savedGrade);
        } catch (Exception e) {
            System.err.println("[WARN] Could not send revision request email to teachers: " + e.getMessage());
        }
        
        return new EvaluationGradeDto(savedGrade);
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
        EvaluationGrade savedGrade = evaluationGradeRepository.save(grade);
        
        // Phase 2: Send email notification to head teacher after teacher submits revision
        try {
            sendTeacherRevisionEmailToHeadTeacher(savedGrade);
        } catch (Exception e) {
            System.err.println("[WARN] Could not send teacher revision email to head teacher: " + e.getMessage());
        }
        
        return new EvaluationGradeDto(savedGrade);
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
        
        // Phase 3: Send email notification to student after final decision
        try {
            sendFinalDecisionEmailToStudent(savedGrade, approved);
        } catch (Exception e) {
            System.err.println("[WARN] Could not send final decision email to student: " + e.getMessage());
        }
        
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

    // Phase 1: Send email to all teachers when student requests revision
    private void sendRevisionRequestEmailToTeachers(EvaluationGrade grade) {
        try {
            String studentName = grade.getStudentEnrollment().getStudent().getName();
            String ucName = grade.getStudentEnrollment().getCurricularUnit().getName();
            String evaluationName = grade.getEvaluation().getTitle();
            
            String subject = "Pedido de Revisão de Nota - " + ucName;
            String body = String.format(
                "Caro(a) Docente,\n\n" +
                "O aluno %s solicitou uma revisão de nota para a avaliação '%s' da UC %s.\n\n" +
                "Motivo da revisão: %s\n\n" +
                "Por favor, aceda ao Sistema AMS para analisar e submeter a sua revisão.\n\n" +
                "Atenciosamente,\n\n" +
                "Sistema AMS",
                studentName, evaluationName, ucName, grade.getRevisionReason()
            );
            
            // Get all teachers (main teacher + assistant teachers)
            var curricularUnit = grade.getStudentEnrollment().getCurricularUnit();
            
            // Send to main teacher
            String mainTeacherEmail = curricularUnit.getMainTeacher().getEmail();
            sendEmail(mainTeacherEmail, subject, body);
            
            // Send to all assistant teachers
            for (var assistantTeacher : curricularUnit.getAssistantTeachers()) {
                String assistantEmail = assistantTeacher.getEmail();
                sendEmail(assistantEmail, subject, body);
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send revision request email to teachers: " + e.getMessage());
        }
    }

    // Phase 2: Send email to head teacher when teacher submits revision
    private void sendTeacherRevisionEmailToHeadTeacher(EvaluationGrade grade) {
        try {
            String studentName = grade.getStudentEnrollment().getStudent().getName();
            String ucName = grade.getStudentEnrollment().getCurricularUnit().getName();
            String evaluationName = grade.getEvaluation().getTitle();
            
            String subject = "Revisão de Docente Submetida - " + ucName;
            String body = String.format(
                "Caro(a) Professor(a) Regente,\n\n" +
                "Foi submetida uma revisão por um docente para a nota do aluno %s na avaliação '%s' da UC %s.\n\n" +
                "Nota sugerida: %.1f valores\n" +
                "Justificação: %s\n\n" +
                "Por favor, aceda ao Sistema AMS para tomar uma decisão final sobre esta revisão.\n\n" +
                "Atenciosamente,\n\n" +
                "Sistema AMS",
                studentName, evaluationName, ucName, 
                grade.getTeacherSuggestion(), grade.getTeacherJustification()
            );
            
            // Send only to head teacher (main teacher)
            var curricularUnit = grade.getStudentEnrollment().getCurricularUnit();
            String headTeacherEmail = curricularUnit.getMainTeacher().getEmail();
            sendEmail(headTeacherEmail, subject, body);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send teacher revision email to head teacher: " + e.getMessage());
        }
    }

    // Phase 3: Send email to student after final decision
    private void sendFinalDecisionEmailToStudent(EvaluationGrade grade, Boolean approved) {
        try {
            String studentName = grade.getStudentEnrollment().getStudent().getName();
            String ucName = grade.getStudentEnrollment().getCurricularUnit().getName();
            String evaluationName = grade.getEvaluation().getTitle();
            
            String subject = "Decisão Final de Revisão de Nota - " + ucName;
            String body;
            
            if (approved) {
                body = String.format(
                    "Caro(a) %s,\n\n" +
                    "A sua revisão de nota para a avaliação '%s' da UC %s foi APROVADA.\n\n" +
                    "Nova nota: %.1f valores\n" +
                    "Justificação: %s\n\n" +
                    "A nota foi atualizada no sistema e pode ser consultada no painel 'Minhas UCs' do Sistema AMS.\n\n" +
                    "Atenciosamente,\n\n" +
                    "Sistema AMS",
                    studentName, evaluationName, ucName,
                    grade.getFinalGrade(), grade.getFinalJustification()
                );
            } else {
                body = String.format(
                    "Caro(a) %s,\n\n" +
                    "A sua revisão de nota para a avaliação '%s' da UC %s foi REJEITADA.\n\n" +
                    "Justificação: %s\n\n" +
                    "A nota original mantém-se inalterada. Caso pretenda, pode submeter um novo pedido de revisão.\n\n" +
                    "Atenciosamente,\n\n" +
                    "Sistema AMS",
                    studentName, evaluationName, ucName, grade.getFinalJustification()
                );
            }
            
            // Send to student
            String studentEmail = grade.getStudentEnrollment().getStudent().getEmail();
            sendEmail(studentEmail, subject, body);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send final decision email to student: " + e.getMessage());
        }
    }

    // Generic email sending method
    private void sendEmail(String to, String subject, String body) {
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
            System.err.println("[ERROR] Failed to send email to " + to + ": " + e.getMessage());
        }
    }
    
    private String createHtmlEmailBody(String textBody) {
        // Convert plain text to HTML (preserve line breaks)
        String htmlText = textBody.replace("\n", "<br>");
        
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
                        <p><strong>Sistema de Gestão Académica (AMS)</strong><br>
                        Departamento de Engenharia Informática<br>
                        Instituto Superior Técnico</p>
                    </div>
                </div>
            </body>
            </html>
            """, htmlText);
    }
}
