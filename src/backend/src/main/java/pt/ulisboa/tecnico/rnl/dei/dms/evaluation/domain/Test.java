package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;

import java.time.LocalDateTime;

// Domain class representing a test evaluation
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tests")
public class Test extends Evaluation {

    public Test(String title, LocalDateTime date, Double weight, CurricularUnit curricularUnit, LocalDateTime revisionDeadline) {
        super(title, date, weight, curricularUnit, EvaluationType.TEST, revisionDeadline);
    }

    public Test(String title, LocalDateTime date, Double weight, CurricularUnit curricularUnit, 
                String description, Integer durationMinutes, String location, LocalDateTime revisionDeadline) {
        super(title, date, weight, curricularUnit, EvaluationType.TEST, revisionDeadline);
    }

    /**
     * Checks if the test has already happened
     */
    public boolean isCompleted() {
        return getDate() != null && getDate().isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the test is happening soon (within 24 hours)
     */
    public boolean isUpcoming() {
        if (getDate() == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return getDate().isAfter(now) && getDate().isBefore(now.plusHours(24));
    }
}
