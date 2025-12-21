package logic.engine.utils;

import logic.machine.components.Keyboard;
import logic.machine.components.Rotor;
import logic.exceptions.EnigmaException;

import java.util.List;
import java.util.Map;
import java.io.Serializable;

/**
 * Utility class responsible for formatting the machine configuration (rotors, positions, reflector)
 * into a structured string format, including distance from the notch calculation.
 */
public class CodeFormatter implements  Serializable {

    private final Map<Integer, Rotor> allAvailableRotors;
    private final Keyboard keyboard;

    public CodeFormatter(Map<Integer, Rotor> allAvailableRotors, Keyboard keyboard) {
        this.allAvailableRotors = allAvailableRotors;
        this.keyboard = keyboard;
    }

    // Creates the formatted configuration string required for specs/history.
    // Format: <ID, ID, ID> <Pos(Dist), Pos(Dist)> <ReflectorID>
    public String formatConfiguration(List<Integer> rotorIDs, List<Character> positions, String reflectorID) {
        if (rotorIDs.size() != positions.size())
            return "";
        StringBuilder sb = new StringBuilder();

        // IDs: <ID, ID, ID> (Print Left to Right)
        sb.append("<");
        for (int i = 0; i < rotorIDs.size(); i++) {
            sb.append(rotorIDs.get(i));
            if (i != rotorIDs.size() - 1) sb.append(", ");
        }
        sb.append(">");

        // Positions: <Pos(Dist), Pos(Dist)> (Print Left to Right)
        sb.append("<");
        for (int i = 0; i < rotorIDs.size(); i++){
            int id = rotorIDs.get(i);
            char pos = positions.get(i);
            int dist = calculateDistanceFromNotch(id, pos);

            sb.append(pos).append("(").append(dist).append(")");
            if (i != rotorIDs.size() - 1)
                sb.append(",");
        }
        sb.append(">");

        // Reflector: <ReflectorID>
        sb.append("<").append(reflectorID).append(">");
        return sb.toString();
    }

    // Helper to calculate distance from notch for display purposes.
    private int calculateDistanceFromNotch(int rotorId, char currentPosChar) {
        // Rotor object must be available to get the notch position
        Rotor rotor = allAvailableRotors.get(rotorId);
        if (rotor == null) {
            // Should not happen if validation passed, but defensive programming is good.
            throw new EnigmaException(EnigmaException.ErrorCode.USER_ROTOR_NOT_FOUND, rotorId);
        }

        int notchIndex = rotor.getNotch();
        int currentIndex = keyboard.toIndex(currentPosChar);
        int size = keyboard.size();

        return (notchIndex - currentIndex + size) % size;
    }
}