package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO for conflict notifications
 */
@Getter
@Setter
@NoArgsConstructor
public class ConflictNotificationDto {
    private String date;
    private List<EvaluationConflictDto> evaluations;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EvaluationConflictDto {
        private Long id;
        private String title;
        private String type;
        private String curricularUnitName;
        private String mainTeacherEmail;
    }
}
