package pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.rnl.dei.dms.studentEnrollment.domain.StudentEnrollment;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {
    
    List<StudentEnrollment> findByCurricularUnitId(Long curricularUnitId);
    
    List<StudentEnrollment> findByStudentId(Long studentId);
    
    Optional<StudentEnrollment> findByStudentIdAndCurricularUnitId(Long studentId, Long curricularUnitId);
    
    boolean existsByStudentIdAndCurricularUnitId(Long studentId, Long curricularUnitId);
}
