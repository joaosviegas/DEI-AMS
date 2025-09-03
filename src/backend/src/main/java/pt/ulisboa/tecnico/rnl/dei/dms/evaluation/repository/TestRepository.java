package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Test;

// Repository interface for managing Test entities
@Repository
@Transactional
public interface TestRepository extends JpaRepository<Test, Long> {
    
    // Find all tests for a specific curricular unit
    List<Test> findByCurricularUnitId(Long curricularUnitId);
    
    // Find all tests for a specific curricular unit ordered by date
    List<Test> findByCurricularUnitIdOrderByDateAsc(Long curricularUnitId);
    
    // Find upcoming tests (future dates)
    @Query("SELECT t FROM Test t WHERE t.date > :now ORDER BY t.date ASC")
    List<Test> findUpcomingTests(@Param("now") LocalDateTime now);
    
    // Find tests by curricular unit and date range
    @Query("SELECT t FROM Test t WHERE t.curricularUnit.id = :curricularUnitId AND t.date BETWEEN :startDate AND :endDate ORDER BY t.date ASC")
    List<Test> findByCurricularUnitAndDateRange(@Param("curricularUnitId") Long curricularUnitId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    // Check if a curricular unit has tests
    boolean existsByCurricularUnitId(Long curricularUnitId);
    
    // Find tests by title (case insensitive)
    List<Test> findByTitleContainingIgnoreCase(String title);
}
