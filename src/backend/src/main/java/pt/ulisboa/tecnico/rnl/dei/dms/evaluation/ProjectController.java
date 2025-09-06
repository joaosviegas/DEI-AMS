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
        
        // Store the actual file using FileService
        String storedFilename = projectService.storeSubmissionFile(file);
        
        return projectService.submitProject(projectId, studentId, file.getOriginalFilename(), 
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

    @GetMapping("/submissions/{submissionId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadSubmission(@PathVariable long submissionId) {
        return projectService.downloadSubmission(submissionId);
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
        
        // Store the actual file using FileService
        String storedFilename = projectService.storeSubmissionFile(file);
        
        return projectService.submitProjectForGroup(projectId, groupId, studentId, 
                                                  file.getOriginalFilename(), storedFilename, 
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
