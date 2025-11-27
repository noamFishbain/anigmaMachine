package logic.machine;

import logic.loader.dto.MachineDescriptor;

/**
 * Represents the complete Enigma machine.
 * Coordinates the flow of characters through the rotors, reflector,
 * and plugboard (when implemented).
 * Handles stepping logic and provides the main encryption interface.
 */
public class EnigmaMachine {

    private int processedMessages = 0;

    //Tal i made it because i need it in Enigmaengineimpl class
    public EnigmaMachine(MachineDescriptor descriptor) {
        // TODO: build internal machine model from descriptor:
        //  - alphabet
        //  - rotors
        //  - reflectors
        // For now, we only initialize the message counter.
        this.processedMessages = 0;
    }

    public int getProcessedMessages() {
        return processedMessages;
    }

    public void incrementProcessedMessages() {
        processedMessages++;
    }

}
