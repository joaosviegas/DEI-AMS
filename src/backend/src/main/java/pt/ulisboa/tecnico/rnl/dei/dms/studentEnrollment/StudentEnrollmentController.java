package pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.dto.StudentEnrollmentDto;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.service.StudentEnrollmentService;

@RestController
public class StudentEnrollmentController {
    
    @Autowired
    private StudentEnrollmentService studentEnrollmentService;

    @GetMapping("/curricular-units/{curricularUnitId}/enrollments")
    public List<StudentEnrollmentDto> getEnrollmentsByCurricularUnit(@PathVariable long curricularUnitId) {
        return studentEnrollmentService.getStudentEnrollmentsByCurricularUnit(curricularUnitId);
    }

    @GetMapping("/students/{studentId}/enrollments")
    public List<StudentEnrollmentDto> getEnrollmentsByStudent(@PathVariable long studentId) {
        return studentEnrollmentService.getStudentEnrollmentsByStudent(studentId);
    }

    @PostMapping("/student-enrollments")
    public StudentEnrollmentDto enrollStudent(
            @RequestParam long curricularUnitId,
            @RequestParam long studentId,
            @RequestParam(defaultValue = "ENROLLED") String status) {
        return studentEnrollmentService.enrollStudent(studentId, curricularUnitId, status);
    }

    @PutMapping("/student-enrollments/{enrollmentId}/status")
    public StudentEnrollmentDto updateEnrollmentStatus(
            @PathVariable long enrollmentId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        return studentEnrollmentService.updateEnrollmentStatus(enrollmentId, status, reason);
    }

    @PutMapping("/student-enrollments/{enrollmentId}/complete")
    public StudentEnrollmentDto completeEnrollment(
            @PathVariable long enrollmentId,
            @RequestParam Double grade) {
        return studentEnrollmentService.completeEnrollment(enrollmentId, grade);
    }

    @DeleteMapping("/student-enrollments/{enrollmentId}")
    public void deleteEnrollment(@PathVariable long enrollmentId) {
        studentEnrollmentService.deleteEnrollment(enrollmentId);
    }

    @DeleteMapping("/student-enrollments")
    public void unenrollStudent(
            @RequestParam long curricularUnitId,
            @RequestParam long studentId) {
        studentEnrollmentService.unenrollStudent(studentId, curricularUnitId);
    }
}
