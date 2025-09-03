package pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;
import pt.ulisboa.tecnico.rnl.dei.dms.course.dto.CourseDto;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.dto.StudentEnrollmentDto;

import java.util.Set;
import java.util.stream.Collectors;

// Data Transfer Object for CurricularUnit, to communicate with frontend
public record CurricularUnitDto(
    Long id,
    String code,
    String name,
    String semester,
    Integer ects,
    PersonDto mainTeacher,
    Set<CourseDto> courses,
    Set<PersonDto> assistantTeachers,
    Set<PersonDto> students, 
    Set<StudentEnrollmentDto> studentEnrollments 
) {
    public CurricularUnitDto(CurricularUnit curricularUnit) {
        this(
            curricularUnit.getId(),
            curricularUnit.getCode(),
            curricularUnit.getName(),
            curricularUnit.getSemester().toString(),
            curricularUnit.getEcts(),
            new PersonDto(curricularUnit.getMainTeacher()),
            curricularUnit.getCourses().stream()
                .map(CourseDto::new)
                .collect(Collectors.toSet()),
            curricularUnit.getAssistantTeachers().stream()
                .map(PersonDto::new)
                .collect(Collectors.toSet()),
            curricularUnit.getAllStudents().stream()
                .map(PersonDto::new)
                .collect(Collectors.toSet()),
            curricularUnit.getStudentEnrollments().stream()
                .map(StudentEnrollmentDto::new)
                .collect(Collectors.toSet())
        );
    }
}