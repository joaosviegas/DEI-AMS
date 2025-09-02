package pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.course.domain.Course;

import java.util.HashSet;
import java.util.Set;

// Domain class representing a curricular unit in the system
@Data
@Entity
@Getter
@Setter
@Table(name = "curricular_units")
public class CurricularUnit {

    public enum Semester {
        FIRST,
        SECOND,
        ANNUAL
    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "semester", nullable = false)
    @Enumerated(EnumType.STRING)
    private Semester semester;

    @Column(name = "ects", nullable = false)
    private Integer ects;

    // Many-to-one relationship with the main teacher (Professor Regente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_teacher_id", nullable = false)
    private Person mainTeacher;

    // Many-to-many relationship with courses
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "curricular_unit_courses",
        joinColumns = @JoinColumn(name = "curricular_unit_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    // Many-to-many relationship with assistant teachers
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "curricular_unit_assistant_teachers",
        joinColumns = @JoinColumn(name = "curricular_unit_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> assistantTeachers = new HashSet<>();

    // Many-to-many relationship with students
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "curricular_unit_students",
        joinColumns = @JoinColumn(name = "curricular_unit_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<Person> students = new HashSet<>();

    protected CurricularUnit() {
    }

    public CurricularUnit(String code, String name, Semester semester, Integer ects, Person mainTeacher) {
        this.code = code;
        this.name = name;
        this.semester = semester;
        this.ects = ects;
        this.mainTeacher = mainTeacher;
    }

    // Helper methods for managing relationships
    public void addCourse(Course course) {
        this.courses.add(course);
    }

    public void removeCourse(Course course) {
        this.courses.remove(course);
    }

    public void addAssistantTeacher(Person teacher) {
        if (teacher.getType() == Person.PersonType.TEACHING_ASSISTANT || 
            teacher.getType() == Person.PersonType.MAIN_TEACHER) {
            this.assistantTeachers.add(teacher);
        }
    }

    public void removeAssistantTeacher(Person teacher) {
        this.assistantTeachers.remove(teacher);
    }

    public void addStudent(Person student) {
        if (student.getType() == Person.PersonType.STUDENT) {
            this.students.add(student);
        }
    }

    public void removeStudent(Person student) {
        this.students.remove(student);
    }

    // Business logic methods
    public boolean isMainTeacher(Person person) {
        return this.mainTeacher.equals(person);
    }

    public boolean isAssistantTeacher(Person person) {
        return this.assistantTeachers.contains(person);
    }

    public boolean isStudent(Person person) {
        return this.students.contains(person);
    }

    public boolean hasPermissionToManage(Person person) {
        return isMainTeacher(person);
    }

    public boolean hasPermissionToAssess(Person person) {
        return isMainTeacher(person) || isAssistantTeacher(person);
    }
}