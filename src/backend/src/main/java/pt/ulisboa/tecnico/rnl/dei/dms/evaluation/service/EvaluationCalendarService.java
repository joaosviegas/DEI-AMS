package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.EvaluationCalendarDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ConflictNotificationDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.TestRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository.ProjectRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Test;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Project;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Service for evaluation calendar operations
 */
@Service
@Transactional
public class EvaluationCalendarService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Get all evaluations (tests and projects) for calendar view
     */
    @Transactional(readOnly = true)
    public List<EvaluationCalendarDto> getAllEvaluationsForCalendar() {
        List<EvaluationCalendarDto> evaluations = new ArrayList<>();
        
        // Get all tests
        List<Test> tests = testRepository.findAll();
        for (Test test : tests) {
            EvaluationCalendarDto dto = new EvaluationCalendarDto(
                test.getId(),
                test.getTitle(),
                "TEST",
                test.getDate(),
                test.getWeight(),
                test.getRevisionDeadline(),
                test.getCurricularUnit().getName(),
                test.getCurricularUnit().getCode(),
                test.getCurricularUnit().getSemester().toString(),
                test.getCurricularUnit().getMainTeacher().getName(),
                test.getCurricularUnit().getMainTeacher().getEmail(),
                null // Tests don't have descriptions
            );
            evaluations.add(dto);
        }
        
        // Get all projects
        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            EvaluationCalendarDto dto = new EvaluationCalendarDto(
                project.getId(),
                project.getTitle(),
                "PROJECT",
                project.getSubmissionDeadline(), // Use submission deadline as the "date"
                project.getWeight(),
                project.getRevisionDeadline(),
                project.getCurricularUnit().getName(),
                project.getCurricularUnit().getCode(),
                project.getCurricularUnit().getSemester().toString(),
                project.getCurricularUnit().getMainTeacher().getName(),
                project.getCurricularUnit().getMainTeacher().getEmail(),
                project.getDescription()
            );
            evaluations.add(dto);
        }
        
        return evaluations;
    }

    /**
     * Notify regent teachers about evaluation conflicts
     */
    @Transactional
    public void notifyConflicts(List<ConflictNotificationDto> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) {
            return;
        }
        
        for (ConflictNotificationDto conflict : conflicts) {
            Set<String> notifiedEmails = new HashSet<>();
            
            for (ConflictNotificationDto.EvaluationConflictDto evaluation : conflict.getEvaluations()) {
                String teacherEmail = evaluation.getMainTeacherEmail();
                
                // Only send one email per teacher per conflict
                if (teacherEmail != null && !notifiedEmails.contains(teacherEmail)) {
                    try {
                        sendConflictNotificationEmail(teacherEmail, conflict);
                        notifiedEmails.add(teacherEmail);
                    } catch (Exception e) {
                        System.err.println("[WARN] Could not send conflict notification email to " + teacherEmail + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Send email notification about evaluation conflicts to regent teacher
     */
    private void sendConflictNotificationEmail(String teacherEmail, ConflictNotificationDto conflict) {
        try {
            // Build list of conflicting evaluations
            StringBuilder evaluationsList = new StringBuilder();
            for (ConflictNotificationDto.EvaluationConflictDto evaluation : conflict.getEvaluations()) {
                evaluationsList.append("• ")
                              .append(evaluation.getTitle())
                              .append(" (")
                              .append(evaluation.getCurricularUnitName())
                              .append(" - ")
                              .append(evaluation.getType().equals("TEST") ? "Teste" : "Projeto")
                              .append(")\n");
            }
            
            String subject = "Conflito de Horários Detectado - Sistema AMS";
            String body = String.format(
                "Caro(a) Professor(a) Regente,\n\n" +
                "Foi detectado um conflito de horários no calendário de avaliações para o dia %s.\n\n" +
                "Avaliações em conflito:\n%s\n" +
                "Recomenda-se a coordenação entre os docentes responsáveis para resolver este conflito de horários.\n\n" +
                "Por favor, aceda ao Sistema AMS para consultar mais detalhes e coordenar a resolução.\n\n" +
                "Atenciosamente,\n\n" +
                "Sistema AMS",
                formatConflictDate(conflict.getDate()),
                evaluationsList.toString()
            );
            
            sendEmail(teacherEmail, subject, body);
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send conflict notification email to " + teacherEmail + ": " + e.getMessage());
        }
    }

    /**
     * Format conflict date for display
     */
    private String formatConflictDate(String dateString) {
        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dateString);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return dateString; // Return original string if parsing fails
        }
    }

    /**
     * Generic email sending method (reused from EvaluationGradeService pattern)
     */
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
            
            // Create HTML email with signature
            String htmlBody = createHtmlEmailBody(body);
            msg.setContent(htmlBody, "text/html; charset=utf-8");
            
            msg.setHeader("X-Mailer", "AMS System");
            msg.setSentDate(new java.util.Date());
            jakarta.mail.Transport.send(msg);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    /**
     * Create HTML email body with signature
     */
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
