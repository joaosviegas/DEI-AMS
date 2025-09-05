package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.person.domain.Person;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a project submission.
 * Can be individual or group-based depending on the project configuration.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "project_submissions")
public class ProjectSubmission {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * The project this submission is for
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * The group that made this submission (null for individual projects)
     */
    @ManyToOne
    @JoinColumn(name = "project_group_id")
    private ProjectGroup group;

    /**
     * The student who made the submission (for individual projects or group representative)
     */
    @ManyToOne
    @JoinColumn(name = "submitted_by", nullable = false)
    private Person submittedBy;

    /**
     * Original filename of the submitted file
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * Stored filename (with UUID or similar to avoid conflicts)
     */
    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    /**
     * File size in bytes
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME type of the file
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * File extension
     */
    @Column(name = "file_extension")
    private String fileExtension;

    /**
     * When this submission was made
     */
    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    /**
     * Comments or notes about the submission
     */
    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    /**
     * Automatic grade given by the system
     */
    @Column(name = "automatic_grade")
    private Double automaticGrade;

    /**
     * Feedback from automatic grading system
     */
    @Column(name = "automatic_feedback", columnDefinition = "TEXT")
    private String automaticFeedback;

    /**
     * Whether this submission is the latest/active one
     */
    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest = true;

    /**
     * Constructor for individual project submission
     */
    public ProjectSubmission(Project project, Person submittedBy, String originalFilename, 
                           String storedFilename, Long fileSize, String mimeType, String fileExtension) {
        this.project = project;
        this.submittedBy = submittedBy;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.submissionDate = LocalDateTime.now();
        this.isLatest = true;
    }

    /**
     * Constructor for group project submission
     */
    public ProjectSubmission(Project project, ProjectGroup group, Person submittedBy, 
                           String originalFilename, String storedFilename, Long fileSize, 
                           String mimeType, String fileExtension) {
        this.project = project;
        this.group = group;
        this.submittedBy = submittedBy;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.submissionDate = LocalDateTime.now();
        this.isLatest = true;
    }

    /**
     * Checks if this is a group submission
     */
    public boolean isGroupSubmission() {
        return group != null;
    }

    /**
     * Checks if this is an individual submission
     */
    public boolean isIndividualSubmission() {
        return group == null;
    }

    /**
     * Checks if submission was made before the deadline
     */
    public boolean isSubmittedOnTime() {
        return project.getSubmissionDeadline() == null || 
               submissionDate.isBefore(project.getSubmissionDeadline()) ||
               submissionDate.isEqual(project.getSubmissionDeadline());
    }

    /**
     * Checks if submission was made after the deadline
     */
    public boolean isLate() {
        return !isSubmittedOnTime();
    }

    /**
     * Gets formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectSubmission that = (ProjectSubmission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
