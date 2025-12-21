package logic.loader.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * A high-level container for the entire machine configuration loaded from XML.
 * This DTO passes data from the Loader layer to the Engine layer.
 */
public class MachineDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String alphabet;
    private final List<RotorDescriptor> rotors;
    private final List<ReflectorDescriptor> reflectors;
    private final int rotorsCount;
    private final String plugs;

    public MachineDescriptor(int rotorsCount, List<RotorDescriptor> rotors,
                             List<ReflectorDescriptor> reflectors, String alphabet, String plugs) {
        this.rotorsCount = rotorsCount;
        this.rotors = rotors;
        this.reflectors = reflectors != null ? Collections.unmodifiableList(reflectors) : Collections.emptyList();
        this.alphabet = alphabet;
        this.plugs = plugs;
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

    public int getRotorsCount() {

        return rotorsCount;
    }

    public String getPlugs() {
        return plugs; }
}