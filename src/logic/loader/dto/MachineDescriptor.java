package logic.loader.dto;

import logic.machine.components.Rotor;

import java.util.List;
import java.util.Map;

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

    private  String alphabet;
    private  List<RotorDescriptor> rotors;
    private  List<ReflectorDescriptor> reflectors;
    private  int requiredRotorCount;

    public MachineDescriptor(String alphabet,
                             List<RotorDescriptor> rotors,
                             List<ReflectorDescriptor> reflectors,
                             int requiredRotorCount) {
        this.alphabet = alphabet;
        this.rotors = rotors;
        this.reflectors = reflectors;
        this.requiredRotorCount = requiredRotorCount;
    }

    public MachineDescriptor() {

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

    public void setABC(String alphabet) {
        this.alphabet = alphabet;
    }

    public void setRotors(List<RotorDescriptor> rotors) {
        this.rotors = rotors;
    }
    public void setReflectors(List<ReflectorDescriptor> reflectors) {
        this.reflectors = reflectors;
    }
}
