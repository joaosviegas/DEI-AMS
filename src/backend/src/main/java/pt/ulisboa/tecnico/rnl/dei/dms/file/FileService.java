package pt.ulisboa.tecnico.rnl.dei.dms.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.DEIException;
import pt.ulisboa.tecnico.rnl.dei.dms.exceptions.ErrorMessage;

/**
 * Service class for handling file storage and retrieval.
 * This service provides methods to store files and load files as resources.
 */
@Service
public class FileService {
  @Value("${file.upload-dir:./uploads}")
  private String uploadDir;

  /**
   * Stores a file in the configured upload directory.
   * 
   * @param file the file to be stored
   * @return the generated file name
   * @throws DEIException if the file is empty or if there is an error during storage
   */
  public String storeFile(MultipartFile file) {
    if (file.isEmpty()) {
        throw new DEIException(ErrorMessage.EMPTY_REQUIRED_FIELD, "arquivo");
    }

    try {
      Path uploadPath = Paths.get(uploadDir);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
      Path filePath = uploadPath.resolve(fileName);
      
      Files.copy(file.getInputStream(), filePath);
        
      return fileName;
    } catch (IOException e) {
      throw new RuntimeException("Falha ao armazenar o arquivo", e);
    }
  }

  /**
   * Loads a file as a resource.
   * 
   * @param fileName the name of the file to be loaded
   * @return the file as a Resource
   * @throws DEIException if the file is not found
   */
  public Resource loadFileAsResource(String fileName) {
    try {
      Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
      Resource resource = new UrlResource(filePath.toUri());
      
      if (resource.exists()) {
        return resource;
      } else {
        throw new DEIException(ErrorMessage.RESOURCE_NOT_FOUND, "arquivo " + fileName);
      }
    } catch (Exception e) {
      throw new DEIException(ErrorMessage.RESOURCE_NOT_FOUND, "arquivo " + fileName);
    }
  }
}
