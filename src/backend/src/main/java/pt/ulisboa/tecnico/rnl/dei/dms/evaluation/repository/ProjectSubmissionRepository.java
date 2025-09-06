package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectSubmission;

/**
 * Repository interface for managing ProjectSubmission entities
 */
@Repository
@Transactional
public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission, Long> {
    
    /**
     * Find all submissions for a specific project
     */
    List<ProjectSubmission> findByProjectId(Long projectId);
    
    /**
     * Find all submissions for a specific project ordered by submission date
     */
    List<ProjectSubmission> findByProjectIdOrderBySubmissionDateDesc(Long projectId);
    
    /**
     * Find submissions by a specific student
     */
    List<ProjectSubmission> findBySubmittedByIdOrderBySubmissionDateDesc(Long studentId);
    
    /**
     * Find submissions for a specific group
     */
    List<ProjectSubmission> findByGroupIdOrderBySubmissionDateDesc(Long groupId);
    
    /**
     * Find submissions for a specific project and group
     */
    List<ProjectSubmission> findByProjectIdAndGroupIdOrderBySubmissionDateDesc(Long projectId, Long groupId);
    
    /**
     * Find latest submissions only
     */
    List<ProjectSubmission> findByIsLatestTrueOrderBySubmissionDateDesc();
    
    /**
     * Find latest submission for a project and student (individual projects)
     */
    @Query("SELECT ps FROM ProjectSubmission ps WHERE ps.project.id = :projectId AND ps.submittedBy.id = :studentId AND ps.isLatest = true")
    ProjectSubmission findLatestByProjectIdAndStudentId(@Param("projectId") Long projectId, @Param("studentId") Long studentId);
    
    /**
     * Find latest submission for a project and group (group projects)
     */
    @Query("SELECT ps FROM ProjectSubmission ps WHERE ps.project.id = :projectId AND ps.group.id = :groupId AND ps.isLatest = true")
    ProjectSubmission findLatestByProjectIdAndGroupId(@Param("projectId") Long projectId, @Param("groupId") Long groupId);
    
    /**
     * Find submissions that were made after the deadline
     */
    @Query("SELECT ps FROM ProjectSubmission ps WHERE ps.submissionDate > ps.project.submissionDeadline")
    List<ProjectSubmission> findLateSubmissions();
    
    /**
     * Find submissions with automatic grades
     */
    List<ProjectSubmission> findByAutomaticGradeIsNotNull();
}
