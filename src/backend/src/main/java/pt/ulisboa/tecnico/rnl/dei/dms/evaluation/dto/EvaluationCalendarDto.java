package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for unified evaluation calendar view (tests and projects)
 */
@Getter
@Setter
@NoArgsConstructor
public class EvaluationCalendarDto {
    private Long id;
    private String title;
    private String type; // "TEST" or "PROJECT"
    private LocalDateTime date;
    private Double weight;
    private LocalDateTime revisionDeadline;
    private String curricularUnitName;
    private String curricularUnitCode;
    private String semester;
    private String mainTeacherName;
    private String mainTeacherEmail;
    private String description;

    public EvaluationCalendarDto(Long id, String title, String type, LocalDateTime date, 
                               Double weight, LocalDateTime revisionDeadline, 
                               String curricularUnitName, String curricularUnitCode, 
                               String semester, String mainTeacherName, 
                               String mainTeacherEmail, String description) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.date = date;
        this.weight = weight;
        this.revisionDeadline = revisionDeadline;
        this.curricularUnitName = curricularUnitName;
        this.curricularUnitCode = curricularUnitCode;
        this.semester = semester;
        this.mainTeacherName = mainTeacherName;
        this.mainTeacherEmail = mainTeacherEmail;
        this.description = description;
    }
}
