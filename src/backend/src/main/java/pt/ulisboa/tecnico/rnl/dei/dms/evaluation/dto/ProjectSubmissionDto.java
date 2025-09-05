package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectSubmission;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for ProjectSubmission
 */
public record ProjectSubmissionDto(
    Long id,
    Long projectId,
    String projectTitle,
    Long groupId,
    String groupName,
    PersonDto submittedBy,
    String originalFilename,
    String storedFilename,
    Long fileSize,
    String formattedFileSize,
    String mimeType,
    String fileExtension,
    LocalDateTime submissionDate,
    String comments,
    Double automaticGrade,
    String automaticFeedback,
    Boolean isLatest,
    boolean isGroupSubmission,
    boolean isIndividualSubmission,
    boolean isSubmittedOnTime,
    boolean isLate
) {
    public ProjectSubmissionDto(ProjectSubmission submission) {
        this(
            submission.getId(),
            submission.getProject().getId(),
            submission.getProject().getTitle(),
            submission.getGroup() != null ? submission.getGroup().getId() : null,
            submission.getGroup() != null ? submission.getGroup().getGroupName() : null,
            new PersonDto(submission.getSubmittedBy()),
            submission.getOriginalFilename(),
            submission.getStoredFilename(),
            submission.getFileSize(),
            submission.getFormattedFileSize(),
            submission.getMimeType(),
            submission.getFileExtension(),
            submission.getSubmissionDate(),
            submission.getComments(),
            submission.getAutomaticGrade(),
            submission.getAutomaticFeedback(),
            submission.getIsLatest(),
            submission.isGroupSubmission(),
            submission.isIndividualSubmission(),
            submission.isSubmittedOnTime(),
            submission.isLate()
        );
    }
}
