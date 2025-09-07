package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;

import java.time.LocalDateTime;
import java.util.Objects;

// Entity representing a grade for a specific evaluation and student
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "evaluation_grades")
public class EvaluationGrade {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    @ManyToOne
    @JoinColumn(name = "student_enrollment_id", nullable = false)
    private StudentEnrollment studentEnrollment;

    @Column(name = "grade", nullable = false)
    private Double grade; // Grade from 0.0 to 20.0

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    @Column(name = "revision_requested", nullable = false)
    private Boolean revisionRequested = false;

    @Column(name = "revision_reason")
    private String revisionReason;

    @Column(name = "revision_requested_at")
    private LocalDateTime revisionRequestedAt;

    @Column(name = "teacher_suggestion")
    private Double teacherSuggestion;

    @Column(name = "teacher_justification")
    private String teacherJustification;

    @Column(name = "teacher_revision_submitted_at")
    private LocalDateTime teacherRevisionSubmittedAt;

    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(name = "final_justification")
    private String finalJustification;

    @Column(name = "final_approved")
    private Boolean finalApproved;

    @Column(name = "final_decision_at")
    private LocalDateTime finalDecisionAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revision_status")
    private RevisionStatus revisionStatus = RevisionStatus.NONE;

    public enum RevisionStatus {
        NONE,
        REQUESTED,
        TEACHER_SUBMITTED,
        APPROVED,
        REJECTED
    }

    public EvaluationGrade(Evaluation evaluation, StudentEnrollment studentEnrollment, 
                          Double grade) {
        this.evaluation = evaluation;
        this.studentEnrollment = studentEnrollment;
        this.grade = grade;
        this.gradedAt = LocalDateTime.now();
    }

    /**
     * Request a revision for this grade
     */
    public void requestRevision(String reason) {
        this.revisionRequested = true;
        this.revisionReason = reason;
        this.revisionRequestedAt = LocalDateTime.now();
        this.revisionStatus = RevisionStatus.REQUESTED;
    }

    /**
     * Submit teacher revision with suggested grade and justification
     */
    public void submitTeacherRevision(Double suggestedGrade, String justification) {
        if (!this.revisionRequested) {
            throw new IllegalStateException("No revision requested for this grade");
        }
        this.teacherSuggestion = suggestedGrade;
        this.teacherJustification = justification;
        this.teacherRevisionSubmittedAt = LocalDateTime.now();
        this.revisionStatus = RevisionStatus.TEACHER_SUBMITTED;
    }

    /**
     * Approve final revision and apply the final grade
     */
    public void approveFinalRevision(String finalJustification, Double finalGrade) {
        if (this.revisionStatus != RevisionStatus.TEACHER_SUBMITTED) {
            throw new IllegalStateException("Cannot approve revision without teacher submission");
        }
        this.finalJustification = finalJustification;
        this.finalGrade = finalGrade;
        this.finalApproved = true;
        this.finalDecisionAt = LocalDateTime.now();
        this.revisionStatus = RevisionStatus.APPROVED;
        
        // Update the actual grade to the approved final grade
        this.grade = finalGrade;
        this.gradedAt = LocalDateTime.now();
    }

    /**
     * Reject final revision
     */
    public void rejectFinalRevision(String finalJustification) {
        if (this.revisionStatus != RevisionStatus.TEACHER_SUBMITTED) {
            throw new IllegalStateException("Cannot reject revision without teacher submission");
        }
        this.finalJustification = finalJustification;
        this.finalApproved = false;
        this.finalDecisionAt = LocalDateTime.now();
        
        // Reset to REQUESTED status so teacher can review again
        this.revisionStatus = RevisionStatus.REQUESTED;
        
        // Clear teacher review data so they can submit a new review
        this.teacherSuggestion = null;
        this.teacherJustification = null;
        this.teacherRevisionSubmittedAt = null;
    }

    /**
     * Clear revision request (when resolved)
     */
    public void clearRevisionRequest() {
        this.revisionRequested = false;
        this.revisionReason = null;
        this.revisionRequestedAt = null;
        this.revisionStatus = RevisionStatus.NONE;
    }

    /**
     * Update the grade (for revisions)
     */
    public void updateGrade(Double newGrade) {
        this.grade = newGrade;
        this.gradedAt = LocalDateTime.now();
        clearRevisionRequest(); // Clear any pending revision
    }

    /**
     * Check if grade is passing (>= 10.0 in Portuguese scale)
     */
    public boolean isPassing() {
        return grade != null && grade >= 10.0;
    }

    /**
     * Check if this grade has a pending revision request
     */
    public boolean hasPendingRevision() {
        return revisionStatus == RevisionStatus.REQUESTED || 
               revisionStatus == RevisionStatus.TEACHER_SUBMITTED;
    }

    /**
     * Check if teacher has submitted a revision
     */
    public boolean hasTeacherRevision() {
        return revisionStatus == RevisionStatus.TEACHER_SUBMITTED;
    }

    /**
     * Check if revision has been finalized (approved or rejected)
     */
    public boolean isRevisionFinalized() {
        return revisionStatus == RevisionStatus.APPROVED || 
               revisionStatus == RevisionStatus.REJECTED;
    }

    /**
     * Get the current effective grade (final grade if approved, otherwise original grade)
     */
    public Double getEffectiveGrade() {
        if (revisionStatus == RevisionStatus.APPROVED && finalGrade != null) {
            return finalGrade;
        }
        return grade;
    }

    // Getters for revision workflow fields
    public RevisionStatus getRevisionStatus() {
        return revisionStatus;
    }

    public Double getTeacherSuggestedGrade() {
        return teacherSuggestion;
    }

    public String getTeacherJustification() {
        return teacherJustification;
    }

    public LocalDateTime getTeacherReviewedAt() {
        return teacherRevisionSubmittedAt;
    }

    public Double getFinalGrade() {
        return finalGrade;
    }

    public String getFinalJustification() {
        return finalJustification;
    }

    public LocalDateTime getFinalApprovedAt() {
        return finalDecisionAt;
    }

    public Boolean getFinalApproved() {
        return finalApproved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationGrade that = (EvaluationGrade) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
