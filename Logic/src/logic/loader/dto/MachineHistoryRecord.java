package logic.loader.dto;

/**
 * Represents a single record of a processing action in the Enigma machine.
 * Stores the input, output, time taken, and the configuration snapshot at that moment.
 * This class is immutable.
 */
import java.io.Serializable;

public class MachineHistoryRecord implements Serializable {
    private final String input;
    private final String output;
    private final long timeElapsed; // nano-sec
    private final String appliedConfiguration; // the code that has been in the machine

    public MachineHistoryRecord(String input, String output, long timeElapsed, String appliedConfiguration) {
        this.input = input;
        this.output = output;
        this.timeElapsed = timeElapsed;
        this.appliedConfiguration = appliedConfiguration;
    }

    @Override
    public String toString() {
        return String.format(
                "Configuration: %s%nInput:  <%s>%nOutput: <%s>%nTime:   %d ns%n",
                appliedConfiguration, input, output, timeElapsed
        );
    }
}
