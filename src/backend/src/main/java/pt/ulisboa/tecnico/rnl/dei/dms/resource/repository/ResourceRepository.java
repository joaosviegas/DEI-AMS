package pt.ulisboa.tecnico.rnl.dei.dms.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.ulisboa.tecnico.rnl.dei.dms.resource.domain.Resource;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    @Query("SELECT r FROM Resource r WHERE r.curricularUnit.id = :curricularUnitId")
    List<Resource> findByCurricularUnitId(@Param("curricularUnitId") Long curricularUnitId);

    @Query("SELECT r FROM Resource r WHERE r.curricularUnit.id = :curricularUnitId AND r.resourceType = :resourceType")
    List<Resource> findByCurricularUnitIdAndResourceType(@Param("curricularUnitId") Long curricularUnitId, 
                                                         @Param("resourceType") Resource.ResourceType resourceType);
}
