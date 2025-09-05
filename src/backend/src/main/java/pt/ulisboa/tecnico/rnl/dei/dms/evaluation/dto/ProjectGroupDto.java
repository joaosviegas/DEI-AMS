package pt.ulisboa.tecnico.rnl.dei.dms.evaluation.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.evaluation.domain.ProjectGroup;
import pt.ulisboa.tecnico.rnl.dei.dms.person.dto.PersonDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for ProjectGroup
 */
public record ProjectGroupDto(
    Long id,
    Long projectId,
    String projectTitle,
    String groupName,
    LocalDateTime createdAt,
    Double finalGrade,
    int memberCount,
    int maxGroupSize,
    boolean isFull,
    boolean isEmpty,
    boolean canAddMoreMembers,
    List<PersonDto> members,
    ProjectSubmissionDto latestSubmission
) {
    public ProjectGroupDto(ProjectGroup group) {
        this(
            group.getId(),
            group.getProject().getId(),
            group.getProject().getTitle(),
            group.getGroupName(),
            group.getCreatedAt(),
            group.getFinalGrade(),
            group.getMemberCount(),
            group.getProject().getMaxGroupSize(),
            group.isFull(),
            group.isEmpty(),
            group.canAddMoreMembers(),
            group.getMembers() != null ? 
                group.getMembers().stream().map(member -> new PersonDto(member.getStudent())).toList() : 
                List.of(),
            group.getLatestSubmission() != null ? new ProjectSubmissionDto(group.getLatestSubmission()) : null
        );
    }
}
