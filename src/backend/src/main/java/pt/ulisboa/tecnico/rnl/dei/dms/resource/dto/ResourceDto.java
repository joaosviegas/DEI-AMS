package pt.ulisboa.tecnico.rnl.dei.dms.resource.dto;

import pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource;

import java.time.LocalDateTime;

// Data Transfer Object for Resource
public record ResourceDto(
    Long id,
    String name,
    String fileName,
    Long fileSize,
    LocalDateTime uploadDate,
    Long curricularUnitId,
    String curricularUnitName,
    String resourceType
) {
    public ResourceDto(Resource resource) {
        this(
            resource.getId(),
            resource.getName(),
            resource.getFileName(),
            resource.getFileSize(),
            resource.getUploadDate(),
            resource.getCurricularUnit().getId(),
            resource.getCurricularUnit().getName(),
            resource.getResourceType().name()
        );
    }
}
