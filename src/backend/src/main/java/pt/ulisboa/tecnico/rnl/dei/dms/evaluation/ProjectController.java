package pt.ulisboa.tecnico.rnl.dei.dms.evaluation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ProjectDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ProjectGroupDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto.ProjectSubmissionDto;
import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.service.ProjectService;

import java.io.IOException;
import java.util.UUID;

/**
 * REST controller for Project operations
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    // Project CRUD endpoints

    @GetMapping("/{id}")
    public ProjectDto getProject(@PathVariable long id) {
        return projectService.getProject(id);
    }

    @GetMapping("/curricular-unit/{curricularUnitId}")
    public List<ProjectDto> getProjectsByCurricularUnit(@PathVariable long curricularUnitId) {
        return projectService.getProjectsByCurricularUnit(curricularUnitId);
    }

    @PostMapping("/curricular-unit/{curricularUnitId}")
    public ProjectDto createProject(
            @PathVariable long curricularUnitId,
            @RequestParam String title,
            @RequestParam Double weight,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submissionDeadline,
            @RequestParam String description,
            @RequestParam(required = false) Integer maxGroupSize
            ) {
        return projectService.createProject(curricularUnitId, title, weight,
                submissionDeadline, description, maxGroupSize);
    }

    @PutMapping("/{id}")
    public ProjectDto updateProject(
            @PathVariable long id,
            @RequestParam String title,
            @RequestParam Double weight,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime submissionDeadline,
            @RequestParam String description,
            @RequestParam(required = false) String allowedExtensions,
            @RequestParam(required = false) Long maxFileSize) {
        
        return projectService.updateProject(id, title, weight, submissionDeadline, 
                                          description, allowedExtensions, maxFileSize);
    }

    @DeleteMapping("/{id}")
    public void deleteProject(@PathVariable long id) {
        projectService.deleteProject(id);
    }

    // Group management endpoints

    @GetMapping("/{projectId}/groups")
    public List<ProjectGroupDto> getProjectGroups(@PathVariable long projectId) {
        return projectService.getProjectGroups(projectId);
    }

    @GetMapping("/{projectId}/groups/student/{studentId}")
    public ResponseEntity<ProjectGroupDto> getStudentGroup(
            @PathVariable long projectId, 
            @PathVariable long studentId) {
        
        ProjectGroupDto group = projectService.getStudentGroup(projectId, studentId);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.notFound().build();
    }

    // Submission endpoints

    @PostMapping("/{projectId}/submissions")
    public ProjectSubmissionDto submitProject(
            @PathVariable long projectId,
            @RequestParam long studentId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Generate a unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        
        // In a real implementation, you would:
        // 1. Store the file to disk/cloud storage using storedFilename
        // 2. Return the stored path for later retrieval
        // For now, we'll just pass the parameters to the service
        
        return projectService.submitProject(projectId, studentId, originalFilename, 
                                          storedFilename, file.getSize(), file.getContentType());
    }

    @GetMapping("/{projectId}/submissions")
    public List<ProjectSubmissionDto> getProjectSubmissions(@PathVariable long projectId) {
        return projectService.getProjectSubmissions(projectId);
    }

    @GetMapping("/{projectId}/submissions/student/{studentId}")
    public ResponseEntity<ProjectSubmissionDto> getStudentSubmission(
            @PathVariable long projectId, 
            @PathVariable long studentId) {
        
        ProjectSubmissionDto submission = projectService.getStudentSubmission(projectId, studentId);
        return submission != null ? ResponseEntity.ok(submission) : ResponseEntity.notFound().build();
    }

    // Grading endpoints

    @PostMapping("/submissions/{submissionId}/grade")
    public ProjectSubmissionDto gradeSubmission(
            @PathVariable long submissionId,
            @RequestParam double grade,
            @RequestParam(required = false) String feedback,
            @RequestParam long graderId) {
        
        return projectService.gradeSubmission(submissionId, grade, 
                                            feedback != null ? feedback : "", graderId);
    }

    @DeleteMapping("/submissions/{submissionId}")
    public void deleteSubmission(@PathVariable long submissionId) {
        projectService.deleteSubmission(submissionId);
    }

    // Automatic grading endpoint

    @PostMapping("/{projectId}/auto-grade")
    public ResponseEntity<String> performAutomaticGrading(@PathVariable long projectId) {
        projectService.performAutomaticGrading(projectId);
        return ResponseEntity.ok("Automatic grading completed for project " + projectId);
    }

    // Group-specific submission endpoints

    @GetMapping("/{projectId}/groups/{groupId}/submissions")
    public List<ProjectSubmissionDto> getGroupSubmissions(
            @PathVariable long projectId, 
            @PathVariable long groupId) {
        return projectService.getGroupSubmissions(projectId, groupId);
    }

    @GetMapping("/{projectId}/groups/{groupId}/submissions/latest")
    public ResponseEntity<ProjectSubmissionDto> getLatestGroupSubmission(
            @PathVariable long projectId, 
            @PathVariable long groupId) {
        ProjectSubmissionDto submission = projectService.getLatestGroupSubmission(projectId, groupId);
        return submission != null ? ResponseEntity.ok(submission) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{projectId}/groups/{groupId}/submissions")
    public ProjectSubmissionDto submitProjectForGroup(
            @PathVariable long projectId,
            @PathVariable long groupId,
            @RequestParam long studentId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Generate a unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        
        return projectService.submitProjectForGroup(projectId, groupId, studentId, 
                                                  originalFilename, storedFilename, 
                                                  file.getSize(), file.getContentType());
    }

    // Group grading endpoints

    @PutMapping("/{projectId}/groups/{groupId}/grade")
    public ResponseEntity<String> gradeGroup(
            @PathVariable long projectId,
            @PathVariable long groupId,
            @RequestParam double grade) {
        
        projectService.gradeGroup(projectId, groupId, grade);
        return ResponseEntity.ok("Group grade saved successfully");
    }
}
