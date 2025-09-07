package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// Abstract base class for evaluations (tests and projects)
@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "evaluations")
public abstract class Evaluation {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "weight", nullable = false)
    private Double weight; // Weight in final grade (0.0 to 1.0)

    @ManyToOne
    @JoinColumn(name = "curricular_unit_id", nullable = false)
    private CurricularUnit curricularUnit;

    @Column(name = "evaluation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EvaluationType evaluationType;

    // One-to-many relationship with grades
    @OneToMany(mappedBy = "evaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EvaluationGrade> grades = new HashSet<>();

    @Column(name = "revision_deadline", nullable = false)
    private LocalDateTime revisionDeadline;

    public enum EvaluationType {
        TEST,
        PROJECT
    }

    public Evaluation(String title, LocalDateTime date, Double weight, CurricularUnit curricularUnit, EvaluationType evaluationType, LocalDateTime revisionDeadline) {
        this.title = title;
        this.date = date;
        this.weight = weight;
        this.curricularUnit = curricularUnit;
        this.evaluationType = evaluationType;
        this.revisionDeadline = revisionDeadline;
    }

    /**
     * Validates if the evaluation date is not in the past
     */
    public boolean isDateValid() {
        return date != null && date.isAfter(LocalDateTime.now());
    }

    /**
     * Validates if the weight is within valid range (0.0 to 1.0)
     */
    public boolean isWeightValid() {
        return weight != null && weight >= 0.0 && weight <= 1.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluation that = (Evaluation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}