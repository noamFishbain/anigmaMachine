package dal.repositories;

import dal.models.ProcessingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProcessingRepository extends JpaRepository<ProcessingEntity, UUID> {

    // Finds all processing records for a specific session
    List<ProcessingEntity> findBySessionId(String sessionId);
}