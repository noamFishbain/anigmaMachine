package dal.repositories;

import dal.models.MachineRotorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MachineRotorRepository extends JpaRepository<MachineRotorEntity, UUID> {

    // Finds all rotors belonging to a specific machine ID
    List<MachineRotorEntity> findByMachine_Id(UUID machineId);
}