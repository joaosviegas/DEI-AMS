package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.EvaluationGrade;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

import java.time.LocalDateTime;

// DTO for EvaluationGrade
public class EvaluationGradeDto {
    private Long id;
    private Long evaluationId;
    private Long studentEnrollmentId;
    private PersonDto student;
    private Double grade;
    private LocalDateTime gradedAt;
    private Boolean revisionRequested;
    private String revisionReason;
    private LocalDateTime revisionRequestedAt;

    public EvaluationGradeDto() {}

    public EvaluationGradeDto(EvaluationGrade evaluationGrade) {
        this.id = evaluationGrade.getId();
        this.evaluationId = evaluationGrade.getEvaluation().getId();
        this.studentEnrollmentId = evaluationGrade.getStudentEnrollment().getId();
        this.student = new PersonDto(evaluationGrade.getStudentEnrollment().getStudent());
        this.grade = evaluationGrade.getGrade();
        this.gradedAt = evaluationGrade.getGradedAt();
        this.revisionRequested = evaluationGrade.getRevisionRequested();
        this.revisionReason = evaluationGrade.getRevisionReason();
        this.revisionRequestedAt = evaluationGrade.getRevisionRequestedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }

    public Long getStudentEnrollmentId() { return studentEnrollmentId; }
    public void setStudentEnrollmentId(Long studentEnrollmentId) { this.studentEnrollmentId = studentEnrollmentId; }

    public PersonDto getStudent() { return student; }
    public void setStudent(PersonDto student) { this.student = student; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public Boolean getRevisionRequested() { return revisionRequested; }
    public void setRevisionRequested(Boolean revisionRequested) { this.revisionRequested = revisionRequested; }

    public String getRevisionReason() { return revisionReason; }
    public void setRevisionReason(String revisionReason) { this.revisionReason = revisionReason; }

    public LocalDateTime getRevisionRequestedAt() { return revisionRequestedAt; }
    public void setRevisionRequestedAt(LocalDateTime revisionRequestedAt) { this.revisionRequestedAt = revisionRequestedAt; }
}
