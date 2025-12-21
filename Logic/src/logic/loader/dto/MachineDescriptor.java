package logic.loader.dto;

import java.util.Collections;
import java.util.List;

/**
 * A high-level container for the entire machine configuration loaded from XML.
 * This DTO passes data from the Loader layer to the Engine layer.
 */
public class MachineDescriptor {

    private final String alphabet;
    private final List<RotorDescriptor> rotors;
    private final List<ReflectorDescriptor> reflectors;
    private final int requiredRotorCount;

    public MachineDescriptor(String alphabet, List<RotorDescriptor> rotors,
                             List<ReflectorDescriptor> reflectors, int requiredRotorCount) {
        this.alphabet = alphabet;
        this.rotors = rotors;
        this.reflectors = reflectors != null ? Collections.unmodifiableList(reflectors) : Collections.emptyList();
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
}
