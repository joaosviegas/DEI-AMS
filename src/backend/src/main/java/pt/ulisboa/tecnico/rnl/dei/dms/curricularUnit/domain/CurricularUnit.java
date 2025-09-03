package pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.course.domain.Course;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Domain class representing a curricular unit in the system
@Data
@Entity
@Getter
@Setter
@Table(name = "curricular_units")
public class CurricularUnit {

    public enum Semester {
        FIRST,
        SECOND
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

    // StudentEnrollment is now the single source of truth for student relationships
    @OneToMany(mappedBy = "curricularUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudentEnrollment> studentEnrollments = new HashSet<>();

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
    
    /**
     * Gets all currently enrolled students (active enrollments only)
     */
    public Set<Person> getEnrolledStudents() {
        return studentEnrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == StudentEnrollment.EnrollmentStatus.ENROLLED)
                .map(StudentEnrollment::getStudent)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all students who have successfully completed the curricular unit
     */
    public Set<Person> getPassedStudents() {
        return studentEnrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == StudentEnrollment.EnrollmentStatus.APPROVED)
                .map(StudentEnrollment::getStudent)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all students who have failed the curricular unit
     */
    public Set<Person> getFailedStudents() {
        return studentEnrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == StudentEnrollment.EnrollmentStatus.FAILED)
                .map(StudentEnrollment::getStudent)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all students regardless of enrollment status (for backward compatibility)
     */
    public Set<Person> getAllStudents() {
        return studentEnrollments.stream()
                .map(StudentEnrollment::getStudent)
                .collect(Collectors.toSet());
    }

    /**
     * Enrolls a student in this curricular unit
     */
    public StudentEnrollment enrollStudent(Person student, StudentEnrollment.EnrollmentStatus status) {
        if (student.getType() != Person.PersonType.STUDENT) {
            throw new IllegalArgumentException("Person is not a student");
        }
        
        if (isStudentEnrolled(student)) {
            throw new IllegalStateException("Student is already enrolled");
        }
        
        StudentEnrollment enrollment = new StudentEnrollment(student, this, status);
        this.addStudentEnrollment(enrollment);
        return enrollment;
    }

    // Business logic methods
    public boolean isMainTeacher(Person person) {
        return this.mainTeacher.equals(person);
    }

    public boolean isAssistantTeacher(Person person) {
        return this.assistantTeachers.contains(person);
    }

    public boolean isStudent(Person person) {
        return isStudentEnrolled(person);
    }

    public boolean hasPermissionToManage(Person person) {
        return isMainTeacher(person);
    }

    public boolean hasPermissionToEvaluate(Person person) {
        return isMainTeacher(person) || isAssistantTeacher(person);
    }

    // Helper methods for managing student enrollments
    public void addStudentEnrollment(StudentEnrollment enrollment) {
        this.studentEnrollments.add(enrollment);
        enrollment.setCurricularUnit(this);
    }

    public void removeStudentEnrollment(StudentEnrollment enrollment) {
        this.studentEnrollments.remove(enrollment);
        enrollment.setCurricularUnit(null);
    }

    public boolean isStudentEnrolled(Person student) {
        return this.studentEnrollments.stream()
                .anyMatch(enrollment -> enrollment.getStudent().equals(student));
    }

    public StudentEnrollment getStudentEnrollment(Person student) {
        return this.studentEnrollments.stream()
                .filter(enrollment -> enrollment.getStudent().equals(student))
                .findFirst()
                .orElse(null);
    }
}