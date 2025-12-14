package logic.engine;

import logic.exceptions.EnigmaException;
import logic.loader.MachineConfigLoader;
import logic.loader.XmlMachineConfigLoader;
import logic.loader.dto.MachineHistoryRecord;
import logic.machine.Machine;
import logic.machine.components.Rotor;
import logic.engine.utils.InputParser;
import logic.engine.validation.EnigmaCodeValidator;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the EnigmaEngine interface.
 * This class coordinates between the UI and the internal EnigmaMachine model.
 */
public class EnigmaEngineImpl implements EnigmaEngine {
    private Machine machine; // Runtime machine instance used to actually process text
    private CodeConfiguration originalCode; // The code that was last chosen by the user (manual/automatic)
    private CodeConfiguration currentCode; // The code after rotor stepping during processing
    private final List<MachineHistoryRecord> historyList = new ArrayList<>();
    private transient InputParser parser;
    private transient EnigmaCodeValidator validator;

    public EnigmaEngineImpl() {

        this.machine = null;
        this.parser = new InputParser();
    }

    @Override
    public void loadMachineFromXml(String path) throws Exception {
        MachineConfigLoader loader = new XmlMachineConfigLoader();
        this.machine = loader.load(path);
        this.validator = new EnigmaCodeValidator(this.machine);

        // Reset code information on new load
        this.originalCode = null;
        this.currentCode = null;
        this.historyList.clear();
    }

    // Sets a manual code configuration based on user input
    @Override
    public String setManualCode(String rotorIDsString, String positionsString, int reflectorNum) throws Exception {
        // Check if machine is loaded
        ensureMachineLoaded();

        // Parsing and Basic Validation
        List<Integer> rotorIDs = parser.parseRotorIDs(rotorIDsString);
        String reflectorID = parser.convertIntToRoman(reflectorNum);
        String alphabet = machine.getKeyboard().asString();

        // Validate
        validator.validateAllManualCode(rotorIDs, positionsString, alphabet);

        // Position characters must be in the machine's keyboard
        List<Character> positionsList = positionsString.toUpperCase().chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());


        // Physically configure the machine
        updateEngineConfiguration(rotorIDs, positionsList, reflectorID);

        return formatCode(this.currentCode);
    }

    @Override
    public void setAutomaticCode() {
        // Check if machine is loaded
        ensureMachineLoaded();

        List<Integer> selectedRotorIDs = selectRandomRotors(3);
        List<Character> selectedPositions = selectRandomPositions(3);
        String selectedReflectorID = selectRandomReflector();

        updateEngineConfiguration(selectedRotorIDs, selectedPositions, selectedReflectorID);
    }

    // Returns a summary of the machine's runtime state and configuration details
    @Override
    public MachineSpecs getMachineSpecs() {
        if (machine == null) {
            // Return empty specs if no machine is loaded
            return new MachineSpecs(0, 0, 0, "", "");
        }

        // Create and return the DTO
        return new MachineSpecs(
                machine.getAllRotorsCount(),
                machine.getAllReflectorsCount(),
                machine.getProcessedMessages(),
                formatCode(originalCode),
                formatCode(currentCode)
        );
    }

    // Processes the given text using the Enigma machine
    @Override
    public String process(String text) {
        // Check if machine is loaded
        ensureMachineLoaded();

        if (originalCode == null) { // cannot do P5 before P3 or P4
            throw new EnigmaException(EnigmaException.ErrorCode.CONFIG_NOT_SET);
        }
        // Trim whitespaces (Remove leading/trailing spaces)
        String cleanedText = text.trim();
        // Validate characters against the Alphabet
        // We convert to UpperCase because the machine is case-insensitive, but the keyboard stores Uppercase.
        validateInputCharacters(cleanedText);
        // Capture the configuration BEFORE processing (for history)
        String currentConfigStr = formatCode(currentCode);

        // Start timer
        long start = System.nanoTime();

        // Process the text (Delegate to machine)
        String output = machine.process(text);

        // Stop timer
        long end = System.nanoTime();

        // Update the current code state (Rotor positions changed)
        List<Character> newPositions = machine.getCurrentRotorPositions();
        if (currentCode != null) {
            this.currentCode = this.currentCode.withRotorPositions(newPositions);;
        }

        // Save to history
        long duration = end - start;
        historyList.add(new MachineHistoryRecord(text, output, duration, currentConfigStr));
        return output;
    }

    // Resets the machine to its original configuration
    @Override
    public void reset() {
        // Check if machine is loaded
        ensureMachineLoaded();

        if (originalCode == null) {
            throw new EnigmaException(EnigmaException.ErrorCode.NO_CONFIGURATION_TO_RESET);
        }

        // Reset the Physical Machine
        machine.setConfiguration(
                originalCode.getRotorIdsInOrder(),
                originalCode.getRotorPositions(),
                originalCode.getReflectorId()
        );

        // Reset the Engine State
        this.currentCode = this.originalCode;
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        if (machine != null) {
            machine.setDebugMode(debugMode);
            System.out.println("Debug mode set to: " + debugMode);
        }
    }

    @Override
    public List<MachineHistoryRecord> getHistory() {
        return historyList;
    }

    private void ensureMachineLoaded() {
        if (machine == null) {
            throw new EnigmaException(EnigmaException.ErrorCode.MACHINE_NOT_LOADED);
        }
    }

    private String formatCode(CodeConfiguration config) {
        if (config == null) {
            return "";
        }
        return machine.formatConfiguration(
                config.getRotorIdsInOrder(),
                config.getRotorPositions(),
                config.getReflectorId()
        );
    }

    private void updateEngineConfiguration(List<Integer> rotorIDs, List<Character> positions, String reflectorID) {
        // Physically configure the machine
        machine.setConfiguration(rotorIDs, positions, reflectorID);

        // Save State
        CodeConfiguration newConfig = new CodeConfiguration(rotorIDs, positions, reflectorID);
        this.originalCode = newConfig;
        this.currentCode = newConfig;
    }

    private List<Integer> selectRandomRotors(int count) {
        List<Integer> availableRotorIDs = new ArrayList<>(machine.getAllAvailableRotors().keySet());
        Collections.shuffle(availableRotorIDs);
        return availableRotorIDs.subList(0, count);
    }

    private List<Character> selectRandomPositions(int count) {
        String keyboard = machine.getKeyboard().asString();
        Random random = new Random();
        List<Character> positions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            positions.add(keyboard.charAt(random.nextInt(keyboard.length())));
        }
        return positions;
    }

    private String selectRandomReflector() {
        List<String> availableReflectors = new ArrayList<>(machine.getAllAvailableReflectors().keySet());
        Random random = new Random();
        return availableReflectors.get(random.nextInt(availableReflectors.size()));
    }
    // ------------------- Bonus: Save & Load Game -------------------

    @Override
    public void saveGame(String pathWithoutExtension) throws IOException {
        // 1. Add binary extension to the file path
        String fullPath = pathWithoutExtension + ".dat";

        // 2. Open file for writing (Serialization)
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fullPath))) {
            // 3. Serialize critical objects in order
            out.writeObject(this.machine);
            out.writeObject(this.historyList);
            out.writeObject(this.originalCode);
            out.writeObject(this.currentCode);
        }
    }

    @Override
    public boolean isCodeConfigurationSet() {
        return this.originalCode != null;
    }

    @Override
    public void loadGame(String pathWithoutExtension) throws IOException, ClassNotFoundException {
        // 1. Add binary extension to the file path
        String fullPath = pathWithoutExtension + ".dat";

        // 2. Open file for reading (Deserialization)
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fullPath))) {
            // 3. Deserialize objects in the EXACT same order they were written
            this.machine = (Machine) in.readObject();

            // Handle History List (since it is final, we cannot re-assign it)
            List<MachineHistoryRecord> loadedHistory = (List<MachineHistoryRecord>) in.readObject();
            this.historyList.clear();
            this.historyList.addAll(loadedHistory);

            this.originalCode = (CodeConfiguration) in.readObject();
            this.currentCode = (CodeConfiguration) in.readObject();

            // 4. Re-initialize transient fields (Component restoration)

            // Re-create the validator with the newly loaded machine instance
            this.validator = new EnigmaCodeValidator(this.machine);

            // Re-create the parser if needed (stateless component)
            if (this.parser == null) {
                this.parser = new InputParser();
            }
        }
    }
    // Helper method to check if all characters exist in the alphabet
    private void validateInputCharacters(String text) {
        // We iterate over the input (converted to UpperCase to match the keyboard)
        for (char c : text.toUpperCase().toCharArray()) {
            if (!machine.getKeyboard().contains(c)) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        INPUT_INVALID_CHARACTER,
                        c,machine.getKeyboard().getABC());
            }
        }
    }

}
