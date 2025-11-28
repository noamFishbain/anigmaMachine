/**
 * Implementation of the EnigmaEngine interface.
 * This class coordinates between the UI and the internal EnigmaMachine model.
 * It is responsible for:
 *  - Loading XML configuration
 *  - Exposing machine specifications (MachineSpecs)
 *  - Processing text (encryption/decryption)
 *  - Resetting machine state
 */

package logic.engine;

import logic.loader.MachineConfigLoader;
import logic.loader.XmlMachineConfigLoader;
import logic.loader.dto.MachineDescriptor;
import logic.machine.Machine;
import logic.machine.MachineImpl;

public class EnigmaEngineImpl implements EnigmaEngine {
    private MachineDescriptor descriptor;
    private Machine machine;

    @Override
    public void loadMachineFromXml(String path) throws Exception {
        MachineConfigLoader loader = new XmlMachineConfigLoader();
        this.descriptor = loader.load(path);

        // Build internal machine model from descriptor
        this.machine = new MachineImpl(descriptor);
    }

    /**
     * Returns a summary of the machine's runtime state and configuration details.
     * This is used by the UI to display the "machine status" command.
     */
    @Override
    public MachineSpecs getMachineSpecs() {

        if (descriptor == null || machine == null) {
            throw new IllegalStateException("Machine has not been loaded yet.");
        }
        int totalRotors = descriptor.getRotors().size();
        int totalReflectors = descriptor.getReflectors().size();
        int processedMessages = machine.getProcessedMessages();

        // Code configurations will be implemented later
        String originalCodeCompact = null;
        String currentCodeCompact = null;

        return new MachineSpecs(
                totalRotors,
                totalReflectors,
                processedMessages,
                originalCodeCompact,
                currentCodeCompact
        );
    }

    /**
     * Processes the given text using the Enigma machine.
     * At this stage, encryption logic is not yet implemented, so the method
     * simply validates the machine is loaded, increments the message counter,
     * and returns the input as-is (echo behavior).
     */
    @Override
    public String process(String text) {

        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        // Delegate processing to the machine
        return machine.process(text);
    }

    /**
     * Resets the machine to its original configuration.
     * Since code configuration is not yet implemented, this method currently
     * performs no operation but provides the required API.
     */
    @Override
    public void reset() {
        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }
        machine.resetToInitialCode();
    }
}
