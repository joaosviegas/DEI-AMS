package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain class representing a Project evaluation.
 * Projects can be individual or group-based, with file submissions and automatic evaluation support.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "projects")
public class Project extends Evaluation {

    /**
     * Maximum number of members per group (null or 1 means individual project)
     */
    @Column(name = "max_group_size")
    private Integer maxGroupSize;

    /**
     * Description or instructions for the project
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Allowed file extensions for submissions (comma-separated, e.g., "py,c,zip,java")
     */
    @Column(name = "allowed_extensions")
    private String allowedExtensions;

    /**
     * Maximum file size in bytes for submissions
     */
    @Column(name = "max_file_size")
    private Long maxFileSize;

    /**
     * Submission deadline (can be different from evaluation date)
     */
    @Column(name = "submission_deadline", nullable = false)
    private LocalDateTime submissionDeadline;

    /**
     * Groups associated with this project
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectGroup> groups = new HashSet<>();

    /**
     * Submissions for this project
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectSubmission> submissions = new HashSet<>();

    /**
     * Constructor for individual projects
     */
    public Project(String title, Double weight, CurricularUnit curricularUnit,
                   LocalDateTime submissionDeadline, String description) {
        super(title, submissionDeadline, weight, curricularUnit, EvaluationType.PROJECT);
        this.submissionDeadline = submissionDeadline;
        this.description = description;
        this.maxGroupSize = 1; // Individual project
        this.allowedExtensions = "py,c,zip";
        this.maxFileSize = 10L * 1024 * 1024; // 10MB default
    }

    /**
     * Constructor for group projects
     */
    public Project(String title, Double weight, CurricularUnit curricularUnit,
                   LocalDateTime submissionDeadline, String description, Integer maxGroupSize) {
        super(title, submissionDeadline, weight, curricularUnit, EvaluationType.PROJECT);
        this.submissionDeadline = submissionDeadline;
        this.description = description;
        this.maxGroupSize = maxGroupSize;
        this.allowedExtensions = "py,c,cpp,java,zip,pdf";
        this.maxFileSize = 10L * 1024 * 1024; // 10MB default
    }

    /**
     * Checks if the project is individual (max group size 1 or null)
     */
    public boolean isIndividual() {
        return maxGroupSize == null || maxGroupSize <= 1;
    }

    /**
     * Checks if the project is group-based
     */
    public boolean isGroupProject() {
        return !isIndividual();
    }

    /**
     * Checks if submissions are still allowed (before deadline)
     */
    public boolean isSubmissionOpen() {
        return submissionDeadline != null && LocalDateTime.now().isBefore(submissionDeadline);
    }

    /**
     * Checks if submissions are closed (past deadline)
     */
    public boolean isSubmissionClosed() {
        return !isSubmissionOpen();
    }

    /**
     * Checks if the project has already happened (evaluation date passed)
     */
    public boolean isCompleted() {
        return getDate() != null && getDate().isBefore(LocalDateTime.now());
    }

    /**
     * Checks if the project is happening soon (within 24 hours)
     */
    public boolean isUpcoming() {
        if (getDate() == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return getDate().isAfter(now) && getDate().isBefore(now.plusHours(24));
    }

    /**
     * Validates if a file extension is allowed
     */
    public boolean isFileExtensionAllowed(String extension) {
        if (allowedExtensions == null || extension == null) return false;
        String[] allowed = allowedExtensions.toLowerCase().split(",");
        String ext = extension.toLowerCase().startsWith(".") ? extension.substring(1) : extension;
        for (String allowedExt : allowed) {
            if (allowedExt.trim().equals(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates if a file size is within limits
     */
    public boolean isFileSizeAllowed(long fileSize) {
        return maxFileSize == null || fileSize <= maxFileSize;
    }
}
