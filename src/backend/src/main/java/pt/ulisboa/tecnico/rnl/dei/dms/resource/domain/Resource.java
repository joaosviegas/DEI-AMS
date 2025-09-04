package pt.ulisboa.tecnico.rnl.dei.dms.resource.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import pt.ulisboa.tecnico.rnl.dei.dms.curricularUnit.domain.CurricularUnit;

import java.time.LocalDateTime;
import java.util.Objects;

// Domain class representing a resource (file) uploaded to a curricular unit
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // Original filename

    @Column(name = "file_name", nullable = false)
    private String fileName; // Stored filename (UUID-based)

    @Column(name = "file_size")
    private Long fileSize; // File size in bytes

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "curricular_unit_id", nullable = false)
    private CurricularUnit curricularUnit;

    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    public enum ResourceType {
        MATERIAL, // Study materials uploaded by teachers
        SUBMISSION // Student submissions for projects
    }

    public Resource(String name, String fileName, Long fileSize, CurricularUnit curricularUnit, 
                   ResourceType resourceType) {
        this.name = name;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.curricularUnit = curricularUnit;
        this.resourceType = resourceType;
        this.uploadDate = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return Objects.equals(id, resource.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
