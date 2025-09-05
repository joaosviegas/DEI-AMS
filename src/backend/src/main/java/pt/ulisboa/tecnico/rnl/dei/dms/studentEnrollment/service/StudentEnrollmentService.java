package pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.dto.StudentEnrollmentDto;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.repository.StudentEnrollmentRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.person.repository.PersonRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.repository.CurricularUnitRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service.ProjectService;

// Service class for managing StudentEnrollment entities
@Service
@Transactional
public class StudentEnrollmentService {

    @Autowired
    private StudentEnrollmentRepository studentEnrollmentRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CurricularUnitRepository curricularUnitRepository;

    @Autowired
    private ProjectService projectService;

    private StudentEnrollment fetchStudentEnrollmentOrThrow(long id) {
        return studentEnrollmentRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_STUDENT_ENROLLMENT, Long.toString(id)));
    }

    private Person fetchPersonOrThrow(long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_PERSON, Long.toString(id)));
    }

    private CurricularUnit fetchCurricularUnitOrThrow(long id) {
        return curricularUnitRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_CURRICULAR_UNIT, Long.toString(id)));
    }

    @Transactional
    public List<StudentEnrollmentDto> getStudentEnrollmentsByCurricularUnit(long curricularUnitId) {
        return studentEnrollmentRepository.findByCurricularUnitId(curricularUnitId).stream()
                .map(StudentEnrollmentDto::new)
                .toList();
    }

    @Transactional
    public List<StudentEnrollmentDto> getStudentEnrollmentsByStudent(long studentId) {
        return studentEnrollmentRepository.findByStudentId(studentId).stream()
                .map(StudentEnrollmentDto::new)
                .toList();
    }

    @Transactional
    public StudentEnrollmentDto enrollStudent(long studentId, long curricularUnitId, String status) {
        Person student = fetchPersonOrThrow(studentId);
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);

        // Check if student is actually a student
        if (student.getType() != Person.PersonType.STUDENT) {
            throw new DEIException(ErrorMessage.PERSON_NOT_STUDENT);
        }

        // Check if enrollment already exists
        if (studentEnrollmentRepository.existsByStudentIdAndCurricularUnitId(studentId, curricularUnitId)) {
            throw new DEIException(ErrorMessage.STUDENT_ALREADY_ENROLLED);
        }

        StudentEnrollment.EnrollmentStatus enrollmentStatus;
        try {
            enrollmentStatus = StudentEnrollment.EnrollmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.ENROLLMENT_STATUS_NOT_VALID);
        }

        StudentEnrollment enrollment = new StudentEnrollment(student, curricularUnit, enrollmentStatus);
        
        // Add the enrollment to the curricular unit's collection to maintain bidirectional relationship
        curricularUnit.addStudentEnrollment(enrollment);
        
        StudentEnrollment savedEnrollment = studentEnrollmentRepository.save(enrollment);
        
        // If the student is successfully enrolled and active, integrate them into existing project groups
        if (enrollmentStatus == StudentEnrollment.EnrollmentStatus.ENROLLED) {
            projectService.integrateNewStudentIntoGroups(curricularUnitId, studentId);
        }
        
        return new StudentEnrollmentDto(savedEnrollment);
    }

    @Transactional
    public StudentEnrollmentDto updateEnrollmentStatus(long enrollmentId, String status, String reason) {
        StudentEnrollment enrollment = fetchStudentEnrollmentOrThrow(enrollmentId);

        StudentEnrollment.EnrollmentStatus oldStatus = enrollment.getStatus();

        StudentEnrollment.EnrollmentStatus enrollmentStatus;
        try {
            enrollmentStatus = StudentEnrollment.EnrollmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DEIException(ErrorMessage.ENROLLMENT_STATUS_NOT_VALID);
        }

        enrollment.updateStatus(enrollmentStatus);
        StudentEnrollment savedEnrollment = studentEnrollmentRepository.save(enrollment);
        
        // If the student just became enrolled (wasn't enrolled before), integrate them into project groups
        if (oldStatus != StudentEnrollment.EnrollmentStatus.ENROLLED && 
            enrollmentStatus == StudentEnrollment.EnrollmentStatus.ENROLLED) {
            projectService.integrateNewStudentIntoGroups(
                enrollment.getCurricularUnit().getId(), 
                enrollment.getStudent().getId()
            );
        }
        
        return new StudentEnrollmentDto(savedEnrollment);
    }

    @Transactional
    public StudentEnrollmentDto completeEnrollment(long enrollmentId, Double grade) {
        StudentEnrollment enrollment = fetchStudentEnrollmentOrThrow(enrollmentId);
        
        enrollment.complete(grade);
        return new StudentEnrollmentDto(studentEnrollmentRepository.save(enrollment));
    }

    @Transactional
    public void deleteEnrollment(long enrollmentId) {
        fetchStudentEnrollmentOrThrow(enrollmentId); // ensure exists
        studentEnrollmentRepository.deleteById(enrollmentId);
    }

    @Transactional
    public void unenrollStudent(long studentId, long curricularUnitId) {
        StudentEnrollment enrollment = studentEnrollmentRepository
                .findByStudentIdAndCurricularUnitId(studentId, curricularUnitId)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_STUDENT_ENROLLMENT));
        
        // Remove from the curricular unit's collection to maintain bidirectional relationship
        CurricularUnit curricularUnit = enrollment.getCurricularUnit();
        curricularUnit.removeStudentEnrollment(enrollment);
        
        studentEnrollmentRepository.delete(enrollment);
    }
}
