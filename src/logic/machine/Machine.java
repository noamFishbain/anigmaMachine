package logic.machine;

import logic.engine.MachineSpecs;
import logic.machine.components.Reflector;
import logic.machine.components.Rotor;

import java.util.List;
// import logic.loader.dto.MachineDescriptor; // אם את משתמשת בזה

public interface Machine {

    // Load machine structure from descriptor (rotors, reflector, alphabet, etc.)
    void configure(/* MachineDescriptor descriptor */);

    // Set the initial code (rotor positions, selected reflector, etc.)
    void setInitialCode(/* params for initial code */);

    // Reset machine state back to the initial code
    void resetToInitialCode();

    // Process a full string (encrypt/decrypt)
    String process(String input);

    int getProcessedMessages();

    List<Character> getCurrentRotorPositions();
    // Return machine specs for the UI/engine
    MachineSpecs getSpecs();

    // Configure the active machine components (Rotors and Reflector)
    void setConfiguration(List<Integer> rotorIDs, List<Character> startingPositions, String reflectorID);

    void setDebugMode(boolean debugMode);

    // Getters to check if the machine is configured
    List<Rotor> getActiveRotors();
    Reflector getActiveReflector();
}
