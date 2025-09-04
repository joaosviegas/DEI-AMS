package pt.ulisboa.tecnico.rnl.dei.dms.resource.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.repository.CurricularUnitRepository;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.rnl.dei.dms.file.FileService;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource.ResourceType;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.dto.ResourceDto;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.repository.ResourceRepository;

import java.util.List;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private CurricularUnitRepository curricularUnitRepository;

    @Autowired
    private FileService fileService;

    private CurricularUnit fetchCurricularUnitOrThrow(long id) {
        return curricularUnitRepository.findById(id)
                .orElseThrow(() -> new DEIException(ErrorMessage.NO_SUCH_CURRICULAR_UNIT, String.valueOf(id)));
    }

    @Transactional
    public ResourceDto uploadResource(long curricularUnitId, MultipartFile file, ResourceType resourceType) {
        CurricularUnit curricularUnit = fetchCurricularUnitOrThrow(curricularUnitId);

        // Store the file
        String fileName = fileService.storeFile(file);
        
        // Create resource entity
        pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource resource = 
            new pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource(
                file.getOriginalFilename(),
                fileName,
                file.getSize(),
                curricularUnit,
                resourceType
            );

        return new ResourceDto(resourceRepository.save(resource));
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getResourcesByCurricularUnit(long curricularUnitId) {
        return resourceRepository.findByCurricularUnitId(curricularUnitId).stream()
                .map(ResourceDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getResourcesByCurricularUnitAndType(long curricularUnitId, ResourceType resourceType) {
        return resourceRepository.findByCurricularUnitIdAndResourceType(curricularUnitId, resourceType).stream()
                .map(ResourceDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadResource(long resourceId) {
        pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource resourceEntity = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new DEIException(ErrorMessage.RESOURCE_NOT_FOUND, String.valueOf(resourceId)));

        Resource file = fileService.loadFileAsResource(resourceEntity.getFileName());
        return file;
    }

    @Transactional
    public void deleteResource(long resourceId) {
        resourceRepository.findById(resourceId)
                .orElseThrow(() -> new DEIException(ErrorMessage.RESOURCE_NOT_FOUND, String.valueOf(resourceId)));
        resourceRepository.deleteById(resourceId);
    }
}
