package web.service;

import logic.engine.EnigmaEngine;
import logic.engine.EnigmaEngineImpl;
import logic.loader.XmlMachineConfigLoader;
import logic.machine.Machine;
import org.springframework.stereotype.Service;
import java.io.*;
import logic.loader.dto.MachineDescriptor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class EngineManager {

    private final Map<String, EnigmaEngine> engines = new HashMap<>();

    public void loadEngine(InputStream fileContent) throws Exception {
        // Initialize the loader
        XmlMachineConfigLoader loader = new XmlMachineConfigLoader();

        // Load the machine from the stream
        Machine machine = loader.load(fileContent);

        // Create a new Engine instance with this machine
        EnigmaEngine newEngine = new EnigmaEngineImpl(machine);

        // Get the machine name
        String machineName = machine.getName();

        // Check if a machine with this name already exists
        if (engines.containsKey(machineName)) {
            throw new IllegalArgumentException("A machine with the name '" + machineName + "' already exists");
        }

        // Store the engine
        engines.put(machineName, newEngine);
        System.out.println("Successfully loaded machine: " + machineName);
    }

    // Creates a completely new independent copy of the engine
    public EnigmaEngine createEngineInstance(String machineName) {
        EnigmaEngine originalEngine = engines.get(machineName);
        if (originalEngine == null) {
            throw new IllegalArgumentException("Machine not found: " + machineName);
        }

        // Deep Copy of the engine
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(originalEngine);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);

            return (EnigmaEngine) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone engine", e);
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
}