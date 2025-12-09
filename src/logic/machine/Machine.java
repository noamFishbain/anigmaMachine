package logic.machine;

import logic.engine.MachineSpecs;
import logic.machine.components.Keyboard;
import logic.machine.components.Reflector;
import logic.machine.components.Rotor;

import java.util.List;
import java.util.Map;

public interface Machine {

    // Load machine structure from descriptor (rotors, reflector, alphabet, etc.)
    void configure(/* MachineDescriptor descriptor */);

    // Set the initial code (rotor positions, selected reflector, etc.)
    // void setInitialCode(/* params for initial code */);

    // Reset machine state back to the initial code
    // void resetToInitialCode();

    // Process a full string (encrypt/decrypt)
    String process(String input);

    int getProcessedMessages();

    List<Character> getCurrentRotorPositions();
    // Return machine specs for the UI/engine
    MachineSpecs getMachineSpecs();

    // Configure the active machine components (Rotors and Reflector)
    void setConfiguration(List<Integer> rotorIDs, List<Character> startingPositions, String reflectorID);

    void setDebugMode(boolean debugMode);

    // Getters to check if the machine is configured
    List<Rotor> getActiveRotors();
    Reflector getActiveReflector();
    String formatConfiguration(List<Integer> rotorIDs, List<Character> positions, String reflectorID);
    int getAllRotorsCount();
    public int getAllReflectorsCount();



    // Getters for ALL available components
    Map<Integer, Rotor> getAllAvailableRotors();
    Map<String, Reflector> getAllAvailableReflectors();

    // Allows the engine/UI to access the keyboard for ABC validation
    Keyboard getKeyboard();
}
