package logic.engine;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.Serializable;
import logic.exceptions.EnigmaException;

/** Represents a single code configuration of the Enigma machine.*/
public class CodeConfiguration implements Serializable {

    private final List<Integer> rotorIdsInOrder; // Rotor IDs from LEFT to RIGHT
    private final List<Character> rotorPositions; // Starting positions of each rotor, same order as rotorIdsInOrder
    private final String reflectorId; // Reflector identifier, Roman numeral identifier
    private final String plugs; // Plugboard connections

    // Creates a new CodeConfiguration.
    public CodeConfiguration(List<Integer> rotorIdsInOrder, List<Character> rotorPositions, String reflectorId, String plugs) {
        validateInputs(rotorIdsInOrder, rotorPositions, reflectorId);

        // Defensive copies ensure immutability even if the original list is modified outside
        this.rotorIdsInOrder = Collections.unmodifiableList(new ArrayList<>(rotorIdsInOrder));
        this.rotorPositions = Collections.unmodifiableList(new ArrayList<>(rotorPositions));
        this.reflectorId = reflectorId;
        this.plugs = plugs;
    }

    private void validateInputs(List<Integer> rotorIds, List<Character> positions, String refId) {
        // Basic null checks
        if (rotorIds == null || positions == null || refId == null) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    CONFIG_ARGS_NULL);
        }

        // Size must match: each rotor ID must have a position
        if (rotorIds.size() != positions.size()) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    USER_POSITION_COUNT_MISMATCH,
                    rotorIds.size(),positions.size());
        }
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

    // Returns the plugboard connections string (e.g. "AB|CD"). Returns an empty string if no plugs are configured.
    public String getPlugs() {
        return plugs;
    }

    // Creates a new configuration instance with updated positions, keeping other fields same.
    public CodeConfiguration withRotorPositions(List<Character> newPositions) {
        return new CodeConfiguration(rotorIdsInOrder, newPositions, reflectorId, plugs);
    }

    // Returns a compact string representation required for history/specs. Format: <ID,ID...><Pos,Pos...><ReflectorID>
    public String toCompactString() {
        String base = formatList(rotorIdsInOrder) +
                formatList(rotorPositions) +
                "<" + reflectorId + ">";

        if (plugs != null && !plugs.isEmpty()) {
            base += "<" + plugs + ">";
        }

        return base;
    }

    private String formatList(List<?> list) {
        String content = list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return "<" + content + ">";
    }

    @Override
    public String toString() {
        // For debugging, we simply reuse the compact representation
        return toCompactString();
    }
}
