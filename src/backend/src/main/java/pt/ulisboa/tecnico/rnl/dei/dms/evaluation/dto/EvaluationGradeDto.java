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
    
    // Revision workflow fields
    private String revisionStatus;
    private Double teacherSuggestedGrade;
    private String teacherJustification;
    private LocalDateTime teacherReviewedAt;
    private Double finalGrade;
    private String finalJustification;
    private LocalDateTime finalApprovedAt;
    private Boolean finalApproved;
    
    // Additional fields for frontend
    private String evaluationTitle;
    private String evaluationType;
    private String curricularUnitName;

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
        
        // Revision workflow fields
        this.revisionStatus = evaluationGrade.getRevisionStatus() != null ? evaluationGrade.getRevisionStatus().name() : "NONE";
        this.teacherSuggestedGrade = evaluationGrade.getTeacherSuggestedGrade();
        this.teacherJustification = evaluationGrade.getTeacherJustification();
        this.teacherReviewedAt = evaluationGrade.getTeacherReviewedAt();
        this.finalGrade = evaluationGrade.getFinalGrade();
        this.finalJustification = evaluationGrade.getFinalJustification();
        this.finalApprovedAt = evaluationGrade.getFinalApprovedAt();
        this.finalApproved = evaluationGrade.getFinalApproved();
        
        // Additional fields for frontend compatibility
        this.evaluationTitle = evaluationGrade.getEvaluation().getTitle();
        this.evaluationType = evaluationGrade.getEvaluation().getEvaluationType().name();
        this.curricularUnitName = evaluationGrade.getEvaluation().getCurricularUnit().getName();
    }

    // Additional getters for frontend compatibility
    public String getStudentName() {
        return student != null ? student.name() : null;
    }

    public String getStudentIstId() {
        return student != null ? student.istId() : null;
    }

    public String getEvaluationTitle() {
        return evaluationTitle;
    }

    public String getEvaluationType() {
        return evaluationType;
    }

    public Double getCurrentGrade() {
        return grade;
    }

    public String getStatus() {
        if (revisionStatus == null || "NONE".equals(revisionStatus)) {
            return "AVAILABLE_FOR_REVISION";
        }
        return switch (revisionStatus) {
            case "REQUESTED" -> "REVISION_REQUESTED";
            case "TEACHER_SUBMITTED" -> "AWAITING_FINAL_APPROVAL";
            case "APPROVED" -> "APPROVED";
            case "REJECTED" -> "REJECTED";
            default -> "AVAILABLE_FOR_REVISION";
        };
    }

    public String getCurricularUnitName() {
        return curricularUnitName;
    }

    public String getReason() {
        return revisionReason;
    }

    public Double getSuggestedGrade() {
        return teacherSuggestedGrade;
    }

    public LocalDateTime getRequestedAt() {
        return revisionRequestedAt;
    }

    public LocalDateTime getReviewedAt() {
        return teacherReviewedAt;
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

    // Revision workflow getters and setters
    public String getRevisionStatus() { return revisionStatus; }
    public void setRevisionStatus(String revisionStatus) { this.revisionStatus = revisionStatus; }

    public Double getTeacherSuggestedGrade() { return teacherSuggestedGrade; }
    public void setTeacherSuggestedGrade(Double teacherSuggestedGrade) { this.teacherSuggestedGrade = teacherSuggestedGrade; }

    public String getTeacherJustification() { return teacherJustification; }
    public void setTeacherJustification(String teacherJustification) { this.teacherJustification = teacherJustification; }

    public LocalDateTime getTeacherReviewedAt() { return teacherReviewedAt; }
    public void setTeacherReviewedAt(LocalDateTime teacherReviewedAt) { this.teacherReviewedAt = teacherReviewedAt; }

    public Double getFinalGrade() { return finalGrade; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }

    public String getFinalJustification() { return finalJustification; }
    public void setFinalJustification(String finalJustification) { this.finalJustification = finalJustification; }

    public LocalDateTime getFinalApprovedAt() { return finalApprovedAt; }
    public void setFinalApprovedAt(LocalDateTime finalApprovedAt) { this.finalApprovedAt = finalApprovedAt; }

    public Boolean getFinalApproved() { return finalApproved; }
    public void setFinalApproved(Boolean finalApproved) { this.finalApproved = finalApproved; }

    public void setEvaluationTitle(String evaluationTitle) { this.evaluationTitle = evaluationTitle; }
    
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }
    
    public void setCurricularUnitName(String curricularUnitName) { this.curricularUnitName = curricularUnitName; }
}
