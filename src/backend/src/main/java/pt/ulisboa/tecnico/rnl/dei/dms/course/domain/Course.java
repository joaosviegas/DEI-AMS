package pt.ulisboa.tecnico.rnl.dei.dms.course.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pt.ulisboa.tecnico.rnl.dei.dms.course.dto.CourseDto;

// Domain class representing a course in the system
@Data
@Entity
@Getter
@Setter
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    protected Course() {
    }

    public Course(String code, String name, Integer duration) {
        this.code = code;
        this.name = name;
        this.duration = duration;
    }

    public Course(CourseDto courseDto) {
        this(courseDto.code(), courseDto.name(), courseDto.duration());
    }
}