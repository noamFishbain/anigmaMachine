package service;

import dal.models.MachineEntity;
import dal.models.MachineReflectorEntity;
import dal.models.MachineRotorEntity;
import dal.repositories.MachineReflectorRepository;
import dal.repositories.MachineRepository;
import dal.repositories.MachineRotorRepository;
import logic.loader.dto.MachineDescriptor;
import logic.loader.dto.ReflectorDescriptor;
import logic.loader.dto.RotorDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DBStorageService {

    private final MachineRepository machineRepository;
    private final MachineRotorRepository rotorRepository;
    private final MachineReflectorRepository reflectorRepository;

    @Autowired
    public DBStorageService(MachineRepository machineRepository,
                            MachineRotorRepository rotorRepository,
                            MachineReflectorRepository reflectorRepository) {
        this.machineRepository = machineRepository;
        this.rotorRepository = rotorRepository;
        this.reflectorRepository = reflectorRepository;
    }

    /**
     * Saves the entire machine configuration to the database.
     * This corresponds to Assignment Requirement: Saving system data to Postgres DB.
     */
    @Transactional
    public void saveMachine(MachineDescriptor descriptor) {
        // 1. Check if machine already exists (by name) to prevent duplicates
        if (machineRepository.existsByName(descriptor.getName())) {
            System.out.println("Machine " + descriptor.getName() + " already exists in DB. Skipping save.");
            return;
        }

        // 2. Save the Machine Entity
        MachineEntity machineEntity = new MachineEntity(
                UUID.randomUUID(),
                descriptor.getName(),
                descriptor.getRotorsCount(),
                descriptor.getAlphabet()
        );
        machineRepository.save(machineEntity);

        // 3. Save all Rotors
        for (RotorDescriptor rotorDTO : descriptor.getRotors()) {
            MachineRotorEntity rotorEntity = new MachineRotorEntity(
                    UUID.randomUUID(),
                    machineEntity,
                    rotorDTO.getId(),
                    rotorDTO.getNotchPosition(),
                    rotorDTO.getMapping() // The logic in Entity converts this int[][] to String
            );
            rotorRepository.save(rotorEntity);
        }

        // 4. Save all Reflectors
        for (ReflectorDescriptor reflectorDTO : descriptor.getReflectors()) {
            // Convert List<int[]> to flat int[] array for the Entity
            int[] fullMapping = convertReflectorPairsToMapping(reflectorDTO, descriptor.getAlphabet().length());

            MachineReflectorEntity reflectorEntity = new MachineReflectorEntity(
                    UUID.randomUUID(),
                    machineEntity,
                    reflectorDTO.getId(),
                    fullMapping
            );
            reflectorRepository.save(reflectorEntity);
        }

        System.out.println("Saved machine [" + descriptor.getName() + "] to Database successfully!");
    }

    // Helper to convert your Logic Reflector (list of pairs) to a full mapping array required by the Entity
    private int[] convertReflectorPairsToMapping(ReflectorDescriptor reflectorDTO, int abcSize) {
        int[] mapping = new int[abcSize];
        // Initialize
        for(int i=0; i<abcSize; i++) mapping[i] = i;

        for (int[] pair : reflectorDTO.getPairs()) {
            int input = pair[0];
            int output = pair[1];
            mapping[input] = output;
            mapping[output] = input; // Symmetry
        }
        return mapping;
    }
}