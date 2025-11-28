package logic.machine;

import logic.engine.MachineSpecs;
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

    // Return machine specs for the UI/engine
    MachineSpecs getSpecs();
}
