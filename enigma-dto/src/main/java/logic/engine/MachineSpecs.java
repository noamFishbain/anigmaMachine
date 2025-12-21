package logic.engine;

/**
 * Represents the runtime status of the Enigma machine.
 * This class is a Data Transfer Object (DTO) used by the UI layer to display details
 * without accessing the internal engine logic.
 */
public class MachineSpecs {

    private final int totalRotors;
    private final int totalReflectors;
    private final int totalProcessedMessages;
    private final int requiredRotorsCount;


    private final String originalCodeCompact;  // Example: <1,2,3><A,B,C><I>
    private final String currentCodeCompact;   // Example: <1,2,3><A,D,F><I>

    public MachineSpecs(int totalRotors,
                        int totalReflectors,
                        int totalProcessedMessages,
                        String originalCodeCompact,
                        String currentCodeCompact,
                        int requiredRotorsCount) {

        this.totalRotors = totalRotors;
        this.totalReflectors = totalReflectors;
        this.totalProcessedMessages = totalProcessedMessages;
        this.originalCodeCompact = originalCodeCompact;
        this.currentCodeCompact = currentCodeCompact;
        this.requiredRotorsCount = requiredRotorsCount;
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

    public int getRequiredRotorsCount() {
        return  requiredRotorsCount;
    }
}
