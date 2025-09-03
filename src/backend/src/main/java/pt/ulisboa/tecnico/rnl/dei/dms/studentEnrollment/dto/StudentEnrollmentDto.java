package pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;

import java.time.LocalDate;

public record StudentEnrollmentDto(
    Long id,
    PersonDto student,
    String status,
    LocalDate enrollmentDate,
    LocalDate completionDate,
    Double finalGrade
) {
    public StudentEnrollmentDto(StudentEnrollment enrollment) {
        this(
            enrollment.getId(),
            new PersonDto(enrollment.getStudent()),
            enrollment.getStatus().toString(),
            enrollment.getEnrollmentDate(),
            enrollment.getCompletionDate(),
            enrollment.getFinalGrade()
        );
    }
}
