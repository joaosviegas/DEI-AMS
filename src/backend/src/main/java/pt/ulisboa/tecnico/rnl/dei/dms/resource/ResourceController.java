package pt.ulisboa.tecnico.rnl.dei.dms.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource.ResourceType;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.dto.ResourceDto;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.service.ResourceService;

import java.util.List;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PostMapping("/curricular-unit/{curricularUnitId}/upload")
    public ResourceDto uploadResource(
            @PathVariable long curricularUnitId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "MATERIAL") ResourceType resourceType) {
        return resourceService.uploadResource(curricularUnitId, file, resourceType);
    }

    @GetMapping("/curricular-unit/{curricularUnitId}")
    public List<ResourceDto> getResourcesByCurricularUnit(@PathVariable long curricularUnitId) {
        return resourceService.getResourcesByCurricularUnit(curricularUnitId);
    }

    @GetMapping("/curricular-unit/{curricularUnitId}/type/{resourceType}")
    public List<ResourceDto> getResourcesByCurricularUnitAndType(
            @PathVariable long curricularUnitId,
            @PathVariable ResourceType resourceType) {
        return resourceService.getResourcesByCurricularUnitAndType(curricularUnitId, resourceType);
    }

    @GetMapping("/{resourceId}/download")
    public ResponseEntity<Resource> downloadResource(@PathVariable long resourceId) {
        Resource file = resourceService.downloadResource(resourceId);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @DeleteMapping("/{resourceId}")
    public void deleteResource(@PathVariable long resourceId) {
        resourceService.deleteResource(resourceId);
    }
}
