package logic.engine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single code configuration of the Enigma machine.
 * Defined by:
 *  - The list of rotor ID's used in the machine, in their physical order
 *  - The starting position (window letter) of each rotor
 *  - The selected reflector ID (e.g. "I", "II", "III")
 * This class is immutable and can be safely shared between components.
 */
public class CodeConfiguration {

    private final List<Integer> rotorIdsInOrder; // Rotor ID's in the physical order inside the machine. LEFT to RIGHT order
    private List<Character> rotorPositions; // Starting positions of each rotor, same order as rotorIdsInOrder
    private final String reflectorId; // Reflector identifier, e.g. "I", "II", "III"

    // Creates a new CodeConfiguration.
    public CodeConfiguration(List<Integer> rotorIdsInOrder, List<Character> rotorPositions, String reflectorId) {

        // Basic null checks
        if (rotorIdsInOrder == null || rotorPositions == null || reflectorId == null) {
            throw new IllegalArgumentException("Code configuration arguments cannot be null");
        }

        // Size must match: each rotor ID must have a position
        if (rotorIdsInOrder.size() != rotorPositions.size()) {
            throw new IllegalArgumentException(
                    "Rotor IDs and rotor positions lists must have the same size"
            );
        }

        // Defensive copies to keep this object immutable
        this.rotorIdsInOrder = Collections.unmodifiableList(rotorIdsInOrder);
        this.rotorPositions = Collections.unmodifiableList(rotorPositions);
        this.reflectorId = reflectorId;
    }

    // Returns unmodifiable list of rotor IDs in physical order (left to right)
    public List<Integer> getRotorIdsInOrder() {
        return rotorIdsInOrder;
    }

    // Returns unmodifiable list of rotor starting positions (letters), same order as rotor IDs
    public List<Character> getRotorPositions() {
        return rotorPositions;
    }

    // Returns reflector identifier (Roman numeral string)
    public String getReflectorId() {
        return reflectorId;
    }

    // Creates a new CodeConfiguration with the same rotor IDs and reflector, but with updated rotor positions
    public CodeConfiguration withRotorPositions(List<Character> newPositions) {
        return new CodeConfiguration(rotorIdsInOrder, newPositions, reflectorId);
    }

    // Returns a compact string representation, as required by the assignment.
     /**
     * NOTE:
     *  - We assume rotorIdsInOrder and rotorPositions are not empty.
     *  - We do NOT validate here that letters are part of the machine alphabet.
     *    That validation belongs to higher layers (XML loader / engine).
     */
    public String toCompactString() {
        String rotorsPart = rotorIdsInOrder.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String positionsPart = rotorPositions.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return "<" + rotorsPart + ">" +
                "<" + positionsPart + ">" +
                "<" + reflectorId + ">";
    }

    @Override
    public String toString() {
        // For debugging, we simply reuse the compact representation
        return toCompactString();
    }

    public void setRotorPositions(List<Character> newPositions) {
        this.rotorPositions = newPositions;
    }
}
