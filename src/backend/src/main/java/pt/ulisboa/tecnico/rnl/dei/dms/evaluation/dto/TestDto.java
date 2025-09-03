package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Test;

import java.time.LocalDateTime;

// Data Transfer Object for Test evaluations
public record TestDto(
    Long id,
    String title,
    LocalDateTime date,
    Double weight,
    Long curricularUnitId,
    String curricularUnitName,
    boolean isCompleted,
    boolean isUpcoming
) {
    public TestDto(Test test) {
        this(
            test.getId(),
            test.getTitle(),
            test.getDate(),
            test.getWeight(),
            test.getCurricularUnit().getId(),
            test.getCurricularUnit().getName(),
            test.isCompleted(),
            test.isUpcoming()
        );
    }
}
