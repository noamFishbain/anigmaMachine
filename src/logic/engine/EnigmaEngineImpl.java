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
        this.machine = null;
        this.descriptor = null;
    }

    @Override
    public void loadMachineFromXml(String path) throws Exception {
        MachineConfigLoader loader = new XmlMachineConfigLoader();
        this.machine = loader.load(path);

        // Reset code information on new load
        this.originalCode = null;
        this.currentCode = null;
    }

    // Sets a manual code configuration based on user input
    @Override
    public String setManualCode(String rotorIDsString, String positionsString, int reflectorNum) throws Exception {
        if (machine == null) {
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
        return machine.formatConfiguration(rotorIDs, positionsList, reflectorID);
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
        // Check if machine is loaded
        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded. Please load an XML file first.");
        }

        Random random = new Random();

        // Randomly select 3 unique rotors from available rotors
        List<Integer> availableRotorIDs = new ArrayList<>(machine.getAllAvailableRotors().keySet());
        List<Integer> selectedRotorIDs = new ArrayList<>();

        // Randomly select rotors until we have 3 unique IDs
        while (selectedRotorIDs.size() < 3) {
            int randomIndex = random.nextInt(availableRotorIDs.size());
            Integer id = availableRotorIDs.get(randomIndex);

            if (!selectedRotorIDs.contains(id)) {
                selectedRotorIDs.add(id);
            }
        }

        // Randomly select starting positions
        String alphabet = machine.getKeyboard().asString();
        List<Character> positions = new ArrayList<>();

        for (int i = 0; i < selectedRotorIDs.size(); i++) {
            positions.add(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        // Randomly select a reflector
        List<String> availableReflectors = new ArrayList<>(machine.getAllAvailableReflectors().keySet());
        String selectedReflectorID = availableReflectors.get(random.nextInt(availableReflectors.size()));

        // Configure Machine (Physically set the rotors and reflector)
        machine.setConfiguration(selectedRotorIDs, positions, selectedReflectorID);

        // Save State (Update the engine's current code)
        CodeConfiguration newCode = new CodeConfiguration(selectedRotorIDs, positions, selectedReflectorID);
        this.originalCode = newCode;
        this.currentCode = newCode;
    }

    // Returns a summary of the machine's runtime state and configuration details
    @Override
    public MachineSpecs getMachineSpecs() {
        if (machine == null) {
            // Return empty specs if no machine is loaded
            return new MachineSpecs(0, 0, 0, "", "");
        }

        // 1. Format the string for the Original Code configuration
        String originalCodeFormatted = "";
        if (originalCode != null) {
            // We delegate the formatting logic to the machine, passing the stored config data
            originalCodeFormatted = machine.formatConfiguration(
                    originalCode.getRotorIdsInOrder(),
                    originalCode.getRotorPositions(),
                    originalCode.getReflectorId()
            );
        }

        // 2. Format the string for the Current Code configuration
        String currentCodeFormatted = "";
        if (currentCode != null) {
            // currentCode is updated in process(), so it reflects the live state
            currentCodeFormatted = machine.formatConfiguration(
                    currentCode.getRotorIdsInOrder(),
                    currentCode.getRotorPositions(),
                    currentCode.getReflectorId()
            );
        }

        // 3. Create and return the DTO
        return new MachineSpecs(
                machine.getAllRotorsCount(),      // Ensure you have a getter for this in Machine
                machine.getAllReflectorsCount(),  // Ensure you have a getter for this in Machine
                machine.getProcessedMessages(),
                originalCodeFormatted,
                currentCodeFormatted
        );
    }

    // Processes the given text using the Enigma machine
    @Override
    public String process(String text) {

        if (machine == null) {
            throw new IllegalStateException("Machine is not loaded.");
        }

//        if (machine.getActiveRotors() == null || machine.getActiveReflector() == null) {
//            throw new IllegalStateException("Code configuration must be set (P3 or P4) before processing text.");
//        }

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
        if (originalCode == null) {
            throw new IllegalStateException("No configuration to reset to. Please set code first (P3 or P4).");
        }

        // Reset the Machine (Re-apply the original configuration)
        machine.setConfiguration(
                originalCode.getRotorIdsInOrder(),
                originalCode.getRotorPositions(),
                originalCode.getReflectorId()
        );

        // Reset Engine State
        this.currentCode = originalCode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        if (machine != null) {
            machine.setDebugMode(debugMode);
            System.out.println("Debug mode set to: " + debugMode);
        }
    }
}
