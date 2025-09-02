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

    @PostMapping("/curricular-units/{curricularUnitId}/students/{studentId}")
    public StudentEnrollmentDto enrollStudent(
            @PathVariable long curricularUnitId,
            @PathVariable long studentId,
            @RequestParam(defaultValue = "ENROLLED") String status) {
        return studentEnrollmentService.enrollStudent(studentId, curricularUnitId, status);
    }

    @PutMapping("/enrollments/{enrollmentId}")
    public StudentEnrollmentDto updateEnrollmentStatus(
            @PathVariable long enrollmentId,
            @RequestParam String status) {
        return studentEnrollmentService.updateEnrollmentStatus(enrollmentId, status);
    }

    @DeleteMapping("/enrollments/{enrollmentId}")
    public void deleteEnrollment(@PathVariable long enrollmentId) {
        studentEnrollmentService.deleteEnrollment(enrollmentId);
    }

    @DeleteMapping("/curricular-units/{curricularUnitId}/students/{studentId}")
    public void unenrollStudent(
            @PathVariable long curricularUnitId,
            @PathVariable long studentId) {
        studentEnrollmentService.unenrollStudent(studentId, curricularUnitId);
    }
}
