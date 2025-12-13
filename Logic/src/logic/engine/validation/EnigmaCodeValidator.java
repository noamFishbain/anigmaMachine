package logic.engine.validation;

import logic.machine.Machine;
import logic.machine.components.Rotor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Utility class responsible for validating user-defined code configuration
 * against the rules and the available components of the loaded machine.
 */
public class EnigmaCodeValidator {

    private final Machine machine;

    private static final int REQUIRED_ROTOR_COUNT = 3;

    public EnigmaCodeValidator(Machine machine) {
        this.machine = machine;
    }

    public void validateAllManualCode(List<Integer> rotorIDs, String positionsString, String alphabet) {
        validateRotorCount(rotorIDs);
        validateRotorIDs(rotorIDs);
        validatePositions(positionsString, rotorIDs.size());
        validateCharacter(positionsString, alphabet);
    }

    private void validateRotorCount(List<Integer> rotorIDs) {
        // Check exactly 3 Rotors
        if (rotorIDs.size() != REQUIRED_ROTOR_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "Invalid rotor count. Require exactly %d selected rotors, but got: %d",
                    REQUIRED_ROTOR_COUNT, rotorIDs.size()));
        }
    }

    private void validateRotorIDs(List<Integer> rotorIDs) {
        // Check uniqueness
        if (new HashSet<>(rotorIDs).size() != rotorIDs.size()) {
            throw new IllegalArgumentException("Rotor IDs must be unique. Duplicates found in: " + rotorIDs);
        }

        // Check existence in machine
        Map<Integer, Rotor> availableRotors = machine.getAllAvailableRotors();
        for (int id : rotorIDs) {
            if (!availableRotors.containsKey(id)) {
                throw new IllegalArgumentException("Rotor ID " + id + " does not exist in the machine. Available IDs: " + availableRotors.keySet());
            }
        }
    }

    private void validatePositions(String positionsString, int expectedSize) {
        // Check length
        if (positionsString.length() != expectedSize) {
            throw new IllegalArgumentException(
                    "Rotor count (" + expectedSize + ") must match the number of starting positions (" + positionsString.length() + ")."
            );
        }
    }

    private void validateCharacter(String positionsString, String alphabet) {
        for (char c : positionsString.toUpperCase().toCharArray()) {
            if (alphabet.indexOf(c) == -1) {
                // Improved error message
                throw new IllegalArgumentException("Character '" + c + "' is not part of the machine's keyboard.");
            }
        }
    }
}