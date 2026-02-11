package dal.repositories;

import dal.models.MachineReflectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MachineReflectorRepository extends JpaRepository<MachineReflectorEntity, UUID> {

    // Finds all reflectors belonging to a specific machine ID
    List<MachineReflectorEntity> findByMachine_Id(UUID machineId);
}