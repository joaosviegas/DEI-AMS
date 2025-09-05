package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Project;

/**
 * Repository interface for managing Project entities
 */
@Repository
@Transactional
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Find all projects for a specific curricular unit
     */
    List<Project> findByCurricularUnitId(Long curricularUnitId);
    
    /**
     * Find all projects for a specific curricular unit ordered by date
     */
    List<Project> findByCurricularUnitIdOrderByDateAsc(Long curricularUnitId);
    
    /**
     * Find upcoming projects (future dates)
     */
    @Query("SELECT p FROM Project p WHERE p.date > :now ORDER BY p.date ASC")
    List<Project> findUpcomingProjects(@Param("now") LocalDateTime now);
    
    /**
     * Find projects by curricular unit and date range
     */
    @Query("SELECT p FROM Project p WHERE p.curricularUnit.id = :curricularUnitId AND p.date BETWEEN :startDate AND :endDate ORDER BY p.date ASC")
    List<Project> findByCurricularUnitIdAndDateBetween(@Param("curricularUnitId") Long curricularUnitId, 
                                                       @Param("startDate") LocalDateTime startDate, 
                                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find projects with open submissions
     */
    @Query("SELECT p FROM Project p WHERE p.submissionDeadline > :now ORDER BY p.submissionDeadline ASC")
    List<Project> findProjectsWithOpenSubmissions(@Param("now") LocalDateTime now);
    
    /**
     * Find group projects (maxGroupSize > 1)
     */
    @Query("SELECT p FROM Project p WHERE p.maxGroupSize > 1")
    List<Project> findGroupProjects();
    
    /**
     * Find individual projects (maxGroupSize <= 1 or null)
     */
    @Query("SELECT p FROM Project p WHERE p.maxGroupSize IS NULL OR p.maxGroupSize <= 1")
    List<Project> findIndividualProjects();
}
