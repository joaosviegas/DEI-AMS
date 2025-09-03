package pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;
import pt.ulisboa.tecnico.rnl.dei.dms.course.domain.Course;

// Repository interface for managing CurricularUnit entities
@Repository
@Transactional
public interface CurricularUnitRepository extends JpaRepository<CurricularUnit, Long> {
    Optional<CurricularUnit> findByCode(String code);
    boolean existsByCode(String code);
    
    // Find CUs by main teacher
    List<CurricularUnit> findByMainTeacher(Person mainTeacher);
    
    // Find CUs by course
    List<CurricularUnit> findByCourses(Course course);
    
    // Find CUs by semester
    List<CurricularUnit> findBySemester(CurricularUnit.Semester semester);
    
    // Find CUs where a person is an assistant teacher
    List<CurricularUnit> findByAssistantTeachersContaining(Person person);
    
    // Find CUs where a person is enrolled as a student (through StudentEnrollment)
    @Query("SELECT DISTINCT cu FROM CurricularUnit cu JOIN cu.studentEnrollments se WHERE se.student = :student")
    List<CurricularUnit> findByStudentEnrollments_Student(@Param("student") Person student);
    
    // Eagerly fetch curricular unit with student enrollments
    @Query("SELECT cu FROM CurricularUnit cu LEFT JOIN FETCH cu.studentEnrollments se LEFT JOIN FETCH se.student WHERE cu.id = :id")
    Optional<CurricularUnit> findByIdWithStudentEnrollments(@Param("id") Long id);
    
    // Eagerly fetch all curricular units with student enrollments
    @Query("SELECT DISTINCT cu FROM CurricularUnit cu LEFT JOIN FETCH cu.studentEnrollments se LEFT JOIN FETCH se.student")
    List<CurricularUnit> findAllWithStudentEnrollments();
}
