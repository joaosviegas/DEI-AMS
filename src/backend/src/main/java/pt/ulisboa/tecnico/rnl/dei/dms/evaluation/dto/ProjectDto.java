package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.Project;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Project evaluations
 */
public record ProjectDto(
    Long id,
    String title,
    LocalDateTime date,
    Double weight,
    Long curricularUnitId,
    String curricularUnitName,
    Integer maxGroupSize,
    String description,
    String allowedExtensions,
    Long maxFileSize,
    LocalDateTime submissionDeadline,
    LocalDateTime revisionDeadline,
    boolean isCompleted,
    boolean isUpcoming,
    boolean isIndividual,
    boolean isGroupProject,
    boolean isSubmissionOpen,
    boolean isSubmissionClosed,
    int groupCount,
    int submissionCount
) {
    public ProjectDto(Project project) {
        this(
            project.getId(),
            project.getTitle(),
            project.getDate(),
            project.getWeight(),
            project.getCurricularUnit().getId(),
            project.getCurricularUnit().getName(),
            project.getMaxGroupSize(),
            project.getDescription(),
            project.getAllowedExtensions(),
            project.getMaxFileSize(),
            project.getSubmissionDeadline(),
            project.getRevisionDeadline(),
            project.isCompleted(),
            project.isUpcoming(),
            project.isIndividual(),
            project.isGroupProject(),
            project.isSubmissionOpen(),
            project.isSubmissionClosed(),
            project.getGroups() != null ? project.getGroups().size() : 0,
            project.getSubmissions() != null ? project.getSubmissions().size() : 0
        );
    }
}
