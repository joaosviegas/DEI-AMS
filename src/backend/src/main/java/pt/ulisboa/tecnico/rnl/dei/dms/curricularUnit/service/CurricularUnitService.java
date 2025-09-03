package pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.dto.CurricularUnitDto;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.repository.CurricularUnitRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.course.domain.Course;
import pt.ulisboa.tecnico.rnl.dei.dms.course.repository.CourseRepository;

// Service class for managing CurricularUnit entities
@Service
@Transactional
public class CurricularUnitService {

    @Autowired
    private CurricularUnitRepository curricularUnitRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CourseRepository courseRepository;

    private CurricularUnit fetchCurricularUnitOrThrow(long id) {
        return curricularUnitRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_CURRICULAR_UNIT, Long.toString(id)));
    }

    private Person fetchPersonOrThrow(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id)));
    }

    private Course fetchCourseOrThrow(long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_COURSE, Long.toString(id)));
    }

    @Transactional
    public List<CurricularUnitDto> getCurricularUnits() {
        return curricularUnitRepository.findAll().stream()
                .map(CurricularUnitDto::new)
                .toList();
    }

    @Transactional
    public CurricularUnitDto createCurricularUnit(String code, String name, String semester, 
                                                  Integer ects, Long mainTeacherId) {
        if (code == null || code.isBlank()) {
            throw new DEIException(ErrorMessage.CURRICULAR_UNIT_CODE_NOT_VALID);
            
        } else if (name == null || name.isBlank()) {
            throw new DEIException(ErrorMessage.CURRICULAR_UNIT_NAME_NOT_VALID);

        } else if (curricularUnitRepository.existsByCode(code)) {
            throw new DEIException(ErrorMessage.CURRICULAR_UNIT_ALREADY_EXISTS, code);

        } else if (ects == null || ects <= 0) {
            throw new DEIException(ErrorMessage.CURRICULAR_UNIT_ECTS_NOT_VALID);
        }

        Person mainTeacher = fetchPersonOrThrow(mainTeacherId);
        if (mainTeacher.getType() != Person.PersonType.MAIN_TEACHER && 
            mainTeacher.getType() != Person.PersonType.ADMINISTRATOR) {
            throw new DEIException(ErrorMessage.PERSON_NOT_MAIN_TEACHER);
        }

        CurricularUnit.Semester semesterEnum;
        try {
            semesterEnum = CurricularUnit.Semester.valueOf(semester.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.CURRICULAR_UNIT_SEMESTER_NOT_VALID);
        }
        
        CurricularUnit curricularUnit = new CurricularUnit(code, name, semesterEnum, ects, mainTeacher);
        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    @Transactional
    public CurricularUnitDto getCurricularUnit(long id) {
        return new CurricularUnitDto(fetchCurricularUnitOrThrow(id));
    }

    @Transactional
    public CurricularUnitDto updateCurricularUnit(long id, String code, String name, String semester, 
                                                  Integer ects, Long mainTeacherId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(id);
        Person mainTeacher = fetchPersonOrThrow(mainTeacherId);

        if (mainTeacher.getType() != Person.PersonType.MAIN_TEACHER && 
            mainTeacher.getType() != Person.PersonType.ADMINISTRATOR) {
            throw new DEIException(ErrorMessage.PERSON_NOT_MAIN_TEACHER);
        }

        CurricularUnit.Semester semesterEnum;
        try {
            semesterEnum = CurricularUnit.Semester.valueOf(semester.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.CURRICULAR_UNIT_SEMESTER_NOT_VALID);
        }

        curricularUnit.setCode(code);
        curricularUnit.setName(name);
        curricularUnit.setSemester(semesterEnum);
        curricularUnit.setEcts(ects);
        curricularUnit.setMainTeacher(mainTeacher);

        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    @Transactional
    public void deleteCurricularUnit(long id) {
        fetchCurricularUnitOrThrow(id); // ensure exists
        curricularUnitRepository.deleteById(id);
    }

    // Course management methods
    @Transactional
    public CurricularUnitDto addCourseToCurricularUnit(long curricularUnitId, long courseId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Course course = fetchCourseOrThrow(courseId);
        
        curricularUnit.addCourse(course);
        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    @Transactional
    public CurricularUnitDto removeCourseFromCurricularUnit(long curricularUnitId, long courseId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Course course = fetchCourseOrThrow(courseId);
        
        curricularUnit.removeCourse(course);
        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    // Assistant teacher management methods
    @Transactional
    public CurricularUnitDto addAssistantTeacher(long curricularUnitId, long teacherId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Person teacher = fetchPersonOrThrow(teacherId);
        
        curricularUnit.addAssistantTeacher(teacher);
        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    @Transactional
    public CurricularUnitDto removeAssistantTeacher(long curricularUnitId, long teacherId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Person teacher = fetchPersonOrThrow(teacherId);
        
        curricularUnit.removeAssistantTeacher(teacher);
        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    // Student enrollment methods (using enrollment-based approach)
    // Note: These methods are deprecated. Use StudentEnrollmentService for enrollment management.
    // They are kept for backward compatibility but delegate to the domain model's enrollment methods.
    
    @Transactional
    @Deprecated
    public CurricularUnitDto enrollStudentInCurricularUnit(long curricularUnitId, long studentId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Person student = fetchPersonOrThrow(studentId);
        
        if (student.getType() != Person.PersonType.STUDENT) {
            throw new DEIException(ErrorMessage.PERSON_NOT_STUDENT);
        }
        
        curricularUnit.enrollStudent(student, pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment.EnrollmentStatus.ENROLLED);
        return new CurricularUnitDto(curricularUnitRepository.save(curricularUnit));
    }

    // Permission checking methods
    @Transactional
    public boolean canManageCurricularUnit(long curricularUnitId, long personId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Person person = fetchPersonOrThrow(personId);
        
        return curricularUnit.hasPermissionToManage(person);
    }

    @Transactional
    public boolean canAssessInCurricularUnit(long curricularUnitId, long personId) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);
        Person person = fetchPersonOrThrow(personId);
        
        return curricularUnit.hasPermissionToEvaluate(person);
    }
}