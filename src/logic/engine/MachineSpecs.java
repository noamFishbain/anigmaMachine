package logic.engine;

/**
 * Represents the runtime status of the Enigma machine as required by the assignment.
 * This class is used by the UI layer to display the machine's state without exposing
 * internal implementation details of the engine.
 *
 * It provides:
 *  - The total number of rotors defined in the machine (from the XML file)
 *  - The total number of reflectors defined in the machine (from the XML file)
 *  - The total number of messages processed so far in the current session
 *  - The original code configuration in compact format (e.g., <1,2,3><A,B,C><I>)
 *  - The current code configuration after rotor movements (same compact format)
 *
 * This class serves strictly as a data transfer object (DTO) from the Engine
 * to the UI layer to maintain proper separation of concerns.
 */
public class MachineSpecs {

    private final int totalRotors;
    private final int totalReflectors;
    private final int totalProcessedMessages;

    private final String originalCodeCompact;  // Example: <1,2,3><A,B,C><I>
    private final String currentCodeCompact;   // Example: <1,2,3><A,D,F><I>

    public MachineSpecs(int totalRotors,
                        int totalReflectors,
                        int totalProcessedMessages,
                        String originalCodeCompact,
                        String currentCodeCompact) {

        this.totalRotors = totalRotors;
        this.totalReflectors = totalReflectors;
        this.totalProcessedMessages = totalProcessedMessages;
        this.originalCodeCompact = originalCodeCompact;
        this.currentCodeCompact = currentCodeCompact;
    }

    public int getTotalRotors() {
        return totalRotors;
    }

    public int getTotalReflectors() {
        return totalReflectors;
    }

    public int getTotalProcessedMessages() {
        return totalProcessedMessages;
    }

    public String getOriginalCodeCompact() {
        return originalCodeCompact;
    }

    public String getCurrentCodeCompact() {
        return currentCodeCompact;
    }
}
