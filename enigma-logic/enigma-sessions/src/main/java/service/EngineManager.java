package service;

import logic.engine.EnigmaEngine;
import logic.engine.EnigmaEngineImpl;
import logic.loader.XmlMachineConfigLoader;
import logic.machine.Machine;
import logic.machine.MachineImpl;
import logic.loader.dto.MachineDescriptor;
import org.springframework.stereotype.Service;
import java.io.*;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Acts as the global repository for all loaded Enigma Machine configurations
 */
@Service
public class EngineManager {

    // Map to store multiple engines using the machine name as the key
    private final Map<String, EnigmaEngine> engines = new ConcurrentHashMap<>();

    // Service for handling Database operations (Postgres)
    private final DBStorageService dbStorageService;

    public EngineManager(DBStorageService dbStorageService) {
        this.dbStorageService = dbStorageService;
    }

    // Loads a machine from an XML input stream
    public String loadEngine(InputStream fileContent, String fileName) throws Exception {
        // Initialize the loader
        XmlMachineConfigLoader loader = new XmlMachineConfigLoader();

        // Load the machine descriptor
        MachineDescriptor descriptor = loader.loadDescriptor(fileContent);

        // Get the machine name
        String machineName = descriptor.getName();

        if (machineName == null || machineName.trim().isEmpty()) {
            machineName = fileName != null ? fileName.replace(".xml", "") : "Unknown_Machine_" + System.currentTimeMillis();
            descriptor.setName(machineName); // Update descriptor so DB gets the name too
        }

        // Check if a machine with this name already exists
        if (engines.containsKey(machineName)) {
            throw new IllegalArgumentException("A machine with the name '" + machineName + "' already exists");
        }

        // Try to save the machine to the DB
        try {
            dbStorageService.saveMachine(descriptor);
        } catch (Exception e) {
            System.out.println("Warning: Failed to save to DB (maybe duplicate?): " + e.getMessage());
        }

        // Create the physical machine instance from the descriptor
        Machine machine = new MachineImpl(descriptor);

        // Create a new Engine instance with this machine
        EnigmaEngine newEngine = new EnigmaEngineImpl(machine);

        // Store the engine
        engines.put(machineName, newEngine);

        System.out.println("Successfully loaded machine: " + machineName);
        return machineName;
    }

    // Creates a deep copy of an engine
    public EnigmaEngine createEngineInstance(String machineName) {
        EnigmaEngine originalEngine = engines.get(machineName);
        if (originalEngine == null) {
            throw new IllegalArgumentException("Machine not found: " + machineName);
        }

        // Deep Copy of the engine using Serialization
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(originalEngine);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);

            return (EnigmaEngine) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone engine instance", e);
        }
    }

    // Checks if a machine exists in the repository
    public boolean isMachineExists(String machineName) {

        return engines.containsKey(machineName);
    }

    // Retrieve an engine by its name
    public EnigmaEngine getEngine(String machineName) {

        return engines.get(machineName);
    }

    // Get a set of all loaded machine names
    public Set<String> getMachineNames() {

        return engines.keySet();
    }

    public String getHealthCheck() {

        return "Enigma Server is Up and Running";
    }

    // Get a set of all loaded machine names to verify multi-machine storage
    public Set<String> getLoadedMachineNames() {
        return engines.keySet();
    }
}