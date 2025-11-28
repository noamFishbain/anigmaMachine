package logic.machine;

import logic.engine.MachineSpecs;
import logic.loader.dto.MachineDescriptor;
import logic.machine.components.Keyboard;
import logic.machine.components.Rotor;
import logic.machine.components.Reflector;
import java.util.List;

/**
 * Represents the complete Enigma machine.
 * Coordinates the flow of characters through the rotors, reflector,
 * and plugboard (when implemented).
 * Handles stepping logic and provides the main encryption interface.
 */
public class MachineImpl implements Machine {

    private int processedMessages = 0;
    private Keyboard keyboard;
    private List<Rotor> rotors;
    private Reflector reflector;

    //Tal i made it because i need it in Enigmaengineimpl class
    public MachineImpl(MachineDescriptor descriptor) {
        // TODO: build internal machine model from descriptor:
        //  - keyboard
        //  - rotors
        //  - reflectors
        // For now, we only initialize the message counter.
        this.processedMessages = 0;
    }

    // Returns the number of messages processed so far
    @Override
    public int getProcessedMessages() {

        return processedMessages;
    }

    // Internal helper: increments the message counter. Called automatically inside the process() method
    private void incrementProcessedMessages() {

        processedMessages++;
    }

    // Implement machine setup logic using descriptor data
    @Override
    public void configure() {

    }

    // Implement code initialization logic
    @Override
    public void setInitialCode() {

    }

    // Reset machine to initial configuration
    @Override
    public void resetToInitialCode() {

    }

    // Processes the given text through the machine
    @Override
    public String process(String input) {
        incrementProcessedMessages();
        return input;
    }

    // Returns the machine specifications for display
    @Override
    public MachineSpecs getSpecs() {
        return null;
    }
}
