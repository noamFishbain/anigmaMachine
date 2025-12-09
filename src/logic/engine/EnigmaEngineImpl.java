package logic.engine;

import logic.loader.MachineConfigLoader;
import logic.loader.XmlMachineConfigLoader;
import logic.loader.dto.MachineDescriptor;
import logic.machine.Machine;
import logic.machine.MachineImpl;
import logic.machine.components.Rotor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the EnigmaEngine interface.
 * This class coordinates between the UI and the internal EnigmaMachine model.
 * It is responsible for:
 *  - Loading XML configuration
 *  - Exposing machine specifications (MachineSpecs)
 *  - Processing text (encryption/decryption)
 *  - Resetting machine state
 */
public class EnigmaEngineImpl implements EnigmaEngine {
    private MachineDescriptor descriptor; // Static description of the machine - loaded from XML
    private Machine machine; // Runtime machine instance used to actually process text
    private CodeConfiguration originalCode; // The code that was last chosen by the user (manual/automatic)
    private CodeConfiguration currentCode; // The code after rotor stepping during processing

    public EnigmaEngineImpl() {
        // Default: build a simple hard-coded machine
        this.machine = new MachineImpl();
        this.descriptor = null; // no XML yet
    }

    @Override
    public void loadMachineFromXml(String path) throws Exception {
        MachineConfigLoader loader = new XmlMachineConfigLoader();
        //this.descriptor = (MachineDescriptor) loader.load(path);

        // Build internal machine model from descriptor
        //this.machine = new MachineImpl(descriptor);

        this.machine = loader.load(path);

        // Whenever we load a new machine configuration, we reset code information
        this.originalCode = null;
        this.currentCode = null;
    }

    // Sets a manual code configuration based on user input
    @Override
    public String setManualCode(String rotorIDsString, String positionsString, int reflectorNum) throws Exception {        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded. Please load an XML file first.");
        }

        // Parsing and Basic Validation
        List<Integer> rotorIDs = parseRotorIDs(rotorIDsString);
        String reflectorID = convertIntToRoman(reflectorNum);
        String alphabet = machine.getKeyboard().asString();

        // Validations
        validateRotorCount(rotorIDs);
        validateRotorIDs(rotorIDs);
        validatePositions(positionsString, rotorIDs.size(), alphabet);

        // Position characters must be in the machine's keyboard
        List<Character> positionsList = positionsString.toUpperCase().chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        // Physically configure the machine
        machine.setConfiguration(rotorIDs, positionsList, reflectorID);

        // Create and save the CodeConfiguration object
        CodeConfiguration newCode = new CodeConfiguration(rotorIDs, positionsList, reflectorID);
        this.originalCode = newCode;
        this.currentCode = newCode;
        return newCode.toCompactString();
    }

    private void validateRotorCount(List<Integer> rotorIDs) {
        // Check exactly 3 Rotors
        if (rotorIDs.size() != 3) {
            throw new IllegalArgumentException("Invalid rotor count. Require exactly 3 selected rotors, but got: " + rotorIDs.size());
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

    private void validatePositions(String positionsString, int expectedSize, String alphabet) {
        // Check length
        if (positionsString.length() != expectedSize) {
            throw new IllegalArgumentException(
                    "Rotor count (" + expectedSize + ") must match the number of starting positions (" + positionsString.length() + ")."
            );
        }

        // Check alphabet validity
        for (char pos : positionsString.toUpperCase().toCharArray()) {
            if (alphabet.indexOf(pos) == -1) {
                throw new IllegalArgumentException("Position character '" + pos + "' is not part of the machine's alphabet: " + alphabet);
            }
        }
    }

    // Converts a comma-separated string of rotor IDs into a List of Integers.
    // Also reverses the list because UI input is left to right, but we need right to left order
    private List<Integer> parseRotorIDs(String s) {
        if (s == null || s.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Split, trim, and parse to Integer
        List<Integer> ids = Arrays.stream(s.trim().split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // The order must be reversed for the machine logic
        Collections.reverse(ids);
        return ids;
    }

    // Converts a decimal reflector number (1-5) into its Roman numeral ID ("I"-"V").
    private String convertIntToRoman(int num) {
        if (num < 1 || num > 5) {
            throw new IllegalArgumentException("Reflector selection must be between 1 and 5.");
        }
        String[] roman = {"I", "II", "III", "IV", "V"};
        return roman[num - 1];
    }

    @Override
    public void setAutomaticCode() {
        // TODO (next phases):
        //  - Randomly choose rotor IDs (no duplicates)
        //  - Randomly choose their order
        //  - Randomly choose starting positions (letters from the alphabet)
        //  - Randomly choose a reflector
        //  - Build CodeConfiguration and update originalCode + currentCode
        //
        // For now we just behave like "no code set yet".
        this.originalCode = null;
        this.currentCode = null;
    }

    // Returns a summary of the machine's runtime state and configuration details
    @Override
    public MachineSpecs getMachineSpecs() {
        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        // If we have descriptor from XML – use real values.
        // Otherwise, fall back to the simple machine assumptions (3 rotors, 1 reflector).
        int totalRotors = (machine.getAllAvailableRotors() != null)
                ? machine.getAllAvailableRotors().size()
                : 3; // Fallback for hardcoded machine

        int totalReflectors = (machine.getAllAvailableReflectors() != null)
                ? machine.getAllAvailableReflectors().size()
                : 1; // Fallback for hardcoded machine

        int processedMessages = machine.getProcessedMessages();

        // Convert code configurations into compact string format
        String originalCodeCompact = (originalCode != null)
                ? originalCode.toCompactString()
                : null;

        String currentCodeCompact = (currentCode != null)
                ? currentCode.toCompactString()
                : null;

        return new MachineSpecs(
                totalRotors,
                totalReflectors,
                processedMessages,
                originalCodeCompact,
                currentCodeCompact
        );
    }

    // Processes the given text using the Enigma machine
    @Override
    public String process(String text) {

        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        if (machine.getActiveRotors() == null || machine.getActiveReflector() == null) {
            throw new IllegalStateException("Code configuration must be set (P3 or P4) before processing text.");
        }

        // Delegate processing to the machine
        String output = machine.process(text);

        List<Character> newPositions = machine.getCurrentRotorPositions();

        if (currentCode != null) {
            currentCode.setRotorPositions(newPositions);
        }
        // TODO (later):
        //  - After MachineImpl exposes its current rotor positions,
        //    we will update 'currentCode' here based on the new positions.
        //    Something like:
        //       List<Character> newPositions = machine.getRotorWindowLetters();
        //       if (originalCode != null) {
        //           currentCode = originalCode.withRotorPositions(newPositions);
        //       }

        return output;
    }

    // Resets the machine to its original configuration
    @Override
    public void reset() {
        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

        // Reset machine's internal state
        machine.resetToInitialCode();

        // Reset code configuration: go back from currentCode to originalCode
        if (originalCode != null) {
            currentCode = originalCode;
        } else {
            currentCode = null;
        }
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        if (machine != null) {
            machine.setDebugMode(debugMode);
            System.out.println("Debug mode set to: " + debugMode);
        }
    }
}
