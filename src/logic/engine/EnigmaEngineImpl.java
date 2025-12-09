package logic.engine;

import logic.loader.MachineConfigLoader;
import logic.loader.XmlMachineConfigLoader;
import logic.loader.dto.MachineDescriptor;
import logic.machine.Machine;
import logic.machine.MachineImpl;

import java.util.List;

/**
 * Implementation of the EnigmaEngine interface.
 * This class coordinates between the UI and the internal EnigmaMachine model.
 * It is responsible for:
 *  - Loading XML configuration
 *  - Exposing machine specifications (MachineSpecs)
 *  - Processing text (encryption/decryption)
 *  - Resetting machine state
 */
public class EnigmaEngineImpl implements EnigmaEngine {
    private MachineDescriptor descriptor; // Static description of the machine - loaded from XML
    private Machine machine; // Runtime machine instance used to actually process text
    private CodeConfiguration originalCode; // The code that was last chosen by the user (manual/automatic)
    private CodeConfiguration currentCode; // The code after rotor stepping during processing

    public EnigmaEngineImpl() {
        // Default: build a simple hard-coded machine
        this.machine = new MachineImpl();
        this.descriptor = null; // no XML yet
    }

    @Override
    public void loadMachineFromXml(String path) throws Exception {
        MachineConfigLoader loader = new XmlMachineConfigLoader();
        //this.descriptor = (MachineDescriptor) loader.load(path);

        // Build internal machine model from descriptor
        //this.machine = new MachineImpl(descriptor);

        this.machine = loader.load(path);

        // Whenever we load a new machine configuration, we reset code information
        this.originalCode = null;
        this.currentCode = null;
    }

    @Override
    public void setManualCode() {
        // TODO:
        //  - Read rotor IDs, order, positions and reflector from the UI layer
        //  - Validate against descriptor (rotor IDs, alphabet, reflector ID)
        //  - Build a new CodeConfiguration and update both originalCode and currentCode
        //
        // For the current milestone we simply clear any existing code.
        this.originalCode = null;
        this.currentCode = null;
    }

    @Override
    public void setAutomaticCode() {
        // TODO (next phases):
        //  - Randomly choose rotor IDs (no duplicates)
        //  - Randomly choose their order
        //  - Randomly choose starting positions (letters from the alphabet)
        //  - Randomly choose a reflector
        //  - Build CodeConfiguration and update originalCode + currentCode
        //
        // For now we just behave like "no code set yet".
        this.originalCode = null;
        this.currentCode = null;
    }

    // Returns a summary of the machine's runtime state and configuration details
    @Override
    public MachineSpecs getMachineSpecs() {
        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        // If we have descriptor from XML – use real values.
        // Otherwise, fall back to the simple machine assumptions (3 rotors, 1 reflector).
        int totalRotors = (descriptor != null)
                ? descriptor.getRotors().size()
                : 3;

        int totalReflectors = (descriptor != null)
                ? descriptor.getReflectors().size()
                : 1;

        int processedMessages = machine.getProcessedMessages();

        // Convert code configurations into compact string format
        String originalCodeCompact = (originalCode != null)
                ? originalCode.toCompactString()
                : null;

        String currentCodeCompact = (currentCode != null)
                ? currentCode.toCompactString()
                : null;

        return new MachineSpecs(
                totalRotors,
                totalReflectors,
                processedMessages,
                originalCodeCompact,
                currentCodeCompact
        );
    }

    // Processes the given text using the Enigma machine
    @Override
    public String process(String text) {

        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        // Delegate processing to the machine
        String output = machine.process(text);

        List<Character> newPositions = machine.getCurrentRotorPositions();

        if (currentCode != null) {
            currentCode.setRotorPositions(newPositions);
        }
        // TODO (later):
        //  - After MachineImpl exposes its current rotor positions,
        //    we will update 'currentCode' here based on the new positions.
        //    Something like:
        //       List<Character> newPositions = machine.getRotorWindowLetters();
        //       if (originalCode != null) {
        //           currentCode = originalCode.withRotorPositions(newPositions);
        //       }

        return output;
    }

    // Resets the machine to its original configuration
    @Override
    public void reset() {
        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        // Reset machine's internal state
        machine.resetToInitialCode();

        // Reset code configuration: go back from currentCode to originalCode
        if (originalCode != null) {
            currentCode = originalCode;
        } else {
            currentCode = null;
        }
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        if (machine != null) {
            machine.setDebugMode(debugMode);
            System.out.println("Debug mode set to: " + debugMode);
        }
    }
}
