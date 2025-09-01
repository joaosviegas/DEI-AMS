package pt.ulisboa.tecnico.rnl.dei.dms.course.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.course.domain.Course;

public record CourseDto(
    Long id,
    String code,
    String name,
    Integer duration
) {
    public CourseDto(Course course) {
        this(course.getId(), course.getCode(), course.getName(), course.getDuration());
    }
}