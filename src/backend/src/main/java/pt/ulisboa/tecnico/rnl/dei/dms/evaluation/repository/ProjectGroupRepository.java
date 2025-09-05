package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectGroup;

/**
 * Repository interface for managing ProjectGroup entities
 */
@Repository
@Transactional
public interface ProjectGroupRepository extends JpaRepository<ProjectGroup, Long> {
    
    /**
     * Find all groups for a specific project
     */
    List<ProjectGroup> findByProjectId(Long projectId);
    
    /**
     * Find all groups for a specific project ordered by group name
     */
    List<ProjectGroup> findByProjectIdOrderByGroupNameAsc(Long projectId);
    
    /**
     * Find groups that still have space for more members
     */
    @Query("SELECT pg FROM ProjectGroup pg WHERE SIZE(pg.members) < pg.project.maxGroupSize")
    List<ProjectGroup> findGroupsWithAvailableSlots();
    
    /**
     * Find groups that are full
     */
    @Query("SELECT pg FROM ProjectGroup pg WHERE SIZE(pg.members) >= pg.project.maxGroupSize")
    List<ProjectGroup> findFullGroups();
    
    /**
     * Find empty groups (no members)
     */
    @Query("SELECT pg FROM ProjectGroup pg WHERE SIZE(pg.members) = 0")
    List<ProjectGroup> findEmptyGroups();
    
    /**
     * Find group by project and student
     */
    @Query("SELECT pg FROM ProjectGroup pg JOIN pg.members pm WHERE pg.project.id = :projectId AND pm.student.id = :studentId")
    ProjectGroup findByProjectIdAndStudentId(@Param("projectId") Long projectId, @Param("studentId") Long studentId);
}
