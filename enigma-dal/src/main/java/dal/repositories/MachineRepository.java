package dal.repositories;

import dal.models.MachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MachineRepository extends JpaRepository<MachineEntity, UUID> {

    // Finds a machine by its unique name
    Optional<MachineEntity> findByName(String name);

    // Checks if a machine with this name already exists
    boolean existsByName(String name);
}