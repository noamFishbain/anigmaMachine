package logic.engine;

import logic.engine.utils.InputParser;
import logic.engine.validation.EnigmaCodeValidator;
import logic.exceptions.EnigmaException;
import logic.loader.MachineConfigLoader;
import logic.loader.XmlMachineConfigLoader;
import logic.loader.dto.MachineHistoryRecord;
import logic.machine.Machine;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    private static final int REQUIRED_ROTOR_COUNT_HARDCODED = 3;

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
    public String setManualCode(String rotorIDsString, String positionsString, int reflectorNum, String plugs) throws Exception {
        // Check if machine is loaded
        ensureMachineLoaded();

        // Parse and Validate all inputs against machine rules
        CodeConfiguration initialConfig = parseAndValidateManualInput(rotorIDsString, positionsString, reflectorNum, plugs);

        // Physically configure the machine and update engine state
        updateEngineConfiguration(initialConfig.getRotorIdsInOrder(), initialConfig.getRotorPositions(), initialConfig.getReflectorId(), plugs);

        // Return the formatted current code for UI display
        return formatCode(this.currentCode);
    }

    // Parses the raw strings, converts reflector ID, validates all rules, and returns a CodeConfiguration DTO.
    private CodeConfiguration parseAndValidateManualInput(String rotorIDsString, String positionsString, int reflectorNum, String plugs) throws Exception {

        // Parsing and Basic Conversion
        List<Integer> rotorIDs = parser.parseRotorIDs(rotorIDsString);
        String reflectorID = parser.convertIntToRoman(reflectorNum);
        String alphabet = machine.getKeyboard().asString();

        // Validate against internal Engine/Machine rules (Delegated to Validator)
        validator.validateAllManualCode(rotorIDs, positionsString, alphabet);

        // Prepare the position list for configuration
        // Position characters must be in the machine's keyboard and converted to a list
        List<Character> positionsList = positionsString.toUpperCase().chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        // Create and return the configuration object (CodeConfiguration also validates size match)
        return new CodeConfiguration(rotorIDs, positionsList, reflectorID, plugs);
    }

    @Override
    public void setAutomaticCode() {
        final int count = REQUIRED_ROTOR_COUNT_HARDCODED;

        // Check if machine is loaded
        ensureMachineLoaded();

        List<Integer> selectedRotorIDs = selectRandomRotors(count);
        List<Character> selectedPositions = selectRandomPositions(count);
        String selectedReflectorID = selectRandomReflector();

        updateEngineConfiguration(selectedRotorIDs, selectedPositions, selectedReflectorID, "");
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
        // Pre-process checks (Machine loaded, code set, input characters valid)
        String cleanedText = performPreProcessChecks(text);

        // Delegate the actual processing and time measurement
        String startConfigStr = formatCode(currentCode);

        // Measure time and process text
        long start = System.nanoTime();
        String output = machine.process(cleanedText);
        long end = System.nanoTime();
        long duration = end - start;

        // Update the engine state and save the record to history
        updateStateAndHistory(cleanedText, output, duration, startConfigStr);

        return output;
    }

    // Performs all necessary validation checks before starting the processing
    // Returns the input text ready for processing (trimmed and clean)
    private String performPreProcessChecks(String text) {
        // Check if machine is loaded
        ensureMachineLoaded();

        if (originalCode == null) { // cannot do P5 before P3 or P4
            throw new EnigmaException(EnigmaException.ErrorCode.CONFIG_NOT_SET);
        }

        // Trim whitespaces (Remove leading/trailing spaces)
        String cleanedText = text.trim();

        // Validate characters against the Alphabet
        validateInputCharacters(cleanedText);

        return cleanedText;
    }

    // Updates the current code state (rotor positions) and saves the action to the history log
    private void updateStateAndHistory(String input, String output, long duration, String startConfigStr) {
        // Update the current code state (Rotor positions changed)
        List<Character> newPositions = machine.getCurrentRotorPositions();
        if (currentCode != null) {
            // Creates a new CodeConfiguration instance with the new positions (immutability)
            this.currentCode = this.currentCode.withRotorPositions(newPositions);
        }

        // Save to history
        historyList.add(new MachineHistoryRecord(input, output, duration, startConfigStr));
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
                originalCode.getReflectorId(),
                originalCode.getPlugs()
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
    public int getRequiredRotorCount(){
        return REQUIRED_ROTOR_COUNT_HARDCODED;
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

    private void updateEngineConfiguration(List<Integer> rotorIDs, List<Character> positions, String reflectorID, String plugs) {
        // Physically configure the machine
        machine.setConfiguration(rotorIDs, positions, reflectorID, plugs);

        // Save State
        CodeConfiguration newConfig = new CodeConfiguration(rotorIDs, positions, reflectorID, plugs);
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

    @Override
    public void saveGame(String pathWithoutExtension) throws IOException {
        // Add binary extension to the file path
        String fullPath = pathWithoutExtension + ".dat";

        // Delegate the actual serialization process
        performSerialization(fullPath);
    }

    // Performs the actual writing of the state objects to the file stream.
    private void performSerialization(String fullPath) throws IOException {
        // Open file for writing (Serialization)
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fullPath))) {
            // Serialize critical objects in order
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
        // Add binary extension to the file path
        String fullPath = pathWithoutExtension + ".dat";

        // Perform the actual deserialization and update fields
        performDeserialization(fullPath);

        // Restore transient components
        restoreTransientComponents();
    }

    // Performs the actual reading of the state objects from the file stream and updates the engine fields
    private void performDeserialization(String fullPath) throws IOException, ClassNotFoundException {
        // Open file for reading (Deserialization)
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fullPath))) {
            // Deserialize objects in the EXACT same order they were written
            this.machine = (Machine) in.readObject();

            // Handle History List (since it is final, we cannot re-assign it)
            List<MachineHistoryRecord> loadedHistory = (List<MachineHistoryRecord>) in.readObject();
            this.historyList.clear();
            this.historyList.addAll(loadedHistory);

            this.originalCode = (CodeConfiguration) in.readObject();
            this.currentCode = (CodeConfiguration) in.readObject();
        }
    }

    // Re-initializes non-serialized (transient) utility fields after loading
    private void restoreTransientComponents() {
        // Re-create the validator with the newly loaded machine instance
        this.validator = new EnigmaCodeValidator(this.machine);

        // Re-create the parser if needed (stateless component)
        if (this.parser == null) {
            // Assuming InputParser is stateless and can be recreated easily
            this.parser = new InputParser();
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
