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
    }

    /**
     * Clear revision request (when resolved)
     */
    public void clearRevisionRequest() {
        this.revisionRequested = false;
        this.revisionReason = null;
        this.revisionRequestedAt = null;
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
