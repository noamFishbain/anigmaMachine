package logic.engine.validation;

import logic.exceptions.EnigmaException;
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
            throw new EnigmaException(
                    EnigmaException.ErrorCode.
                            USER_INVALID_ROTOR_COUNT,REQUIRED_ROTOR_COUNT,
                            rotorIDs.size());
        }
    }

    private void validateRotorIDs(List<Integer> rotorIDs) {
        // Check uniqueness
        if (new HashSet<>(rotorIDs).size() != rotorIDs.size()) {
            throw new EnigmaException(EnigmaException.ErrorCode.USER_DUPLICATE_ROTOR_IDS);

        }

        // Check existence in machine
        Map<Integer, Rotor> availableRotors = machine.getAllAvailableRotors();
        for (int id : rotorIDs) {
            if (!availableRotors.containsKey(id)) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        USER_ROTOR_NOT_FOUND
                        ,id,availableRotors.keySet());
            }
        }
    }

    private void validatePositions(String positionsString, int expectedSize) {
        // Check length
        if (positionsString.length() != expectedSize) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    USER_POSITION_COUNT_MISMATCH
                    ,expectedSize,positionsString.length());
        }
    }

    private void validateCharacter(String positionsString, String alphabet) {
        for (char c : positionsString.toUpperCase().toCharArray()) {
            if (alphabet.indexOf(c) == -1) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        USER_INVALID_POSITION_CHAR
                        ,c);
            }
        }
    }
}