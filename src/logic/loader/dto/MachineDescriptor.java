package logic.loader.dto;

import java.util.List;

/**
 * Represents a high-level description of the Enigma machine as loaded
 * from the XML configuration. This DTO is produced by the loader layer
 * and consumed by the engine layer to build the internal machine model.
 *
 * It contains:
 *  - The machine's alphabet
 *  - The list of all rotor definitions
 *  - The list of all reflector definitions
 *  - The number of rotors that must be used in each configuration
 */
public class MachineDescriptor {

    private final String alphabet;
    private final List<RotorDescriptor> rotors;
    private final List<ReflectorDescriptor> reflectors;
    private final int requiredRotorCount;

    public MachineDescriptor(String alphabet,
                             List<RotorDescriptor> rotors,
                             List<ReflectorDescriptor> reflectors,
                             int requiredRotorCount) {
        this.alphabet = alphabet;
        this.rotors = rotors;
        this.reflectors = reflectors;
        this.requiredRotorCount = requiredRotorCount;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public List<RotorDescriptor> getRotors() {
        return rotors;
    }

    public List<ReflectorDescriptor> getReflectors() {
        return reflectors;
    }

    public int getRequiredRotorCount() {
        return requiredRotorCount;
    }
}
