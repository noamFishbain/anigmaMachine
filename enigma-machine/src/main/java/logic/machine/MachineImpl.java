package logic.machine;

import logic.machine.utils.CodeFormatter;
import logic.exceptions.EnigmaException;
import logic.loader.dto.MachineDescriptor;
import logic.loader.dto.ReflectorDescriptor;
import logic.loader.dto.RotorDescriptor;
import logic.machine.components.*;

import java.io.Serializable;
import java.util.*;

public class MachineImpl implements Machine, Serializable {

    private int processedMessages;
    private final Keyboard keyboard;
    private final List<Rotor> activeRotors; // List storing rotors. Index 0 = Rightmost (Fastest), Last Index = Leftmost (Slowest)
    private Reflector activeReflector;
    private final Map<Integer, Rotor> allAvailableRotors;
    private final Map<String, Reflector> allAvailableReflectors;
    private boolean debugMode = false; // Default to true for logs
    private final CodeFormatter formatter;
    private final Plugboard plugboard; // Used for swapping characters before and after the rotors
    private final int rotorsCount;
    // Main constructor from XML Descriptor
    public MachineImpl(MachineDescriptor descriptor) {
        this.keyboard = new KeyboardImpl(descriptor.getAlphabet());
        this.processedMessages = 0;
        this.allAvailableRotors = new HashMap<>();
        this.allAvailableReflectors = new HashMap<>();
        this.activeRotors = new ArrayList<>();
        this.activeReflector = null;
        this.formatter = new CodeFormatter(this.allAvailableRotors, this.keyboard);
        this.plugboard = new PlugboardImpl();
        this.rotorsCount = descriptor.getRotorsCount();


        // Load Rotors
        loadRotors(descriptor.getRotors());

        // Load Reflectors
        loadReflectors(descriptor.getReflectors());
    }

    // Helper method to load rotors from descriptors
    private void loadRotors(List<RotorDescriptor> descriptors) {
        // UPDATED: Now handles the 2D array mapping directly
        for (RotorDescriptor desc : descriptors) {

            // 1. Get the new [ABC][2] location mapping directly from the descriptor
            int[][] mapping = desc.getMapping();

            // 2. Create the Rotor using the updated constructor that accepts int[][]
            // Note: We subtract 1 from the notch position because XML is 1-based, but our internal logic is 0-based.
            Rotor rotor = new RotorImpl(desc.getId(), mapping, desc.getNotchPosition() - 1, 0);

            this.allAvailableRotors.put(rotor.getId(), rotor);
        }
    }

    // Helper method to load reflectors from descriptors
    private void loadReflectors(List<ReflectorDescriptor> descriptors) {
        int keyboardSize = keyboard.size();
        for (ReflectorDescriptor desc : descriptors) {
            int[] mapping = new int[keyboardSize];
            Arrays.fill(mapping, -1);

            for (int[] pair : desc.getPairs()) {
                int input = pair[0];
                int output = pair[1];
                mapping[input] = output;
                mapping[output] = input;
            }
            Reflector reflector = new ReflectorImpl(mapping);
            this.allAvailableReflectors.put(desc.getId(), reflector);
        }
    }

    @Override
    // Processes the entire input string character by character
    public String process(String input) {
        processedMessages++;
        if (input == null || input.isEmpty())
            return "";

        String normalized = input.toUpperCase();
        StringBuilder result = new StringBuilder();

        logDebug("--- [START] Processing String: %s ---", normalized);

        // Ignore characters not in the keyboard alphabet
        for (char c : normalized.toCharArray()) {
            if (!keyboard.contains(c)) {
                result.append(c);
                continue;
            }

            result.append(convert(c));
        }

        logDebug("--- [END] Process Completed. Result: %s ---\n", result.toString());
        return result.toString();
    }

    @Override
    // Handles the complete flow of a single character through the machine
    public char convert(char inputChar) {
        logDebug("\n[CHAR] Processing character: '%c'", inputChar);

        // Plugboard (First Pass)- Before entering the rotors
        char afterPlugboard = plugboard.convert(inputChar);
        logDebug("  [PLUG]  Input '%c' -> Plugboard -> '%c'", inputChar, afterPlugboard);

        // Rotors Logic
        char afterRotors = processRotorsLogic(afterPlugboard);

        // Plugboard (Second Pass)- After exiting the rotors
        char result = plugboard.convert(afterRotors);
        logDebug("  [PLUG]  Rotors '%c' -> Plugboard -> '%c'", afterRotors, result);

        return result;
    }

    // Handles the passage of a character through the rotors and reflector
    private char processRotorsLogic(char input) {
        // Log state before stepping
        logDebug("  [STATE] Rotors BEFORE process (Left->Right): %s", getCurrentRotorPositions());

        // Step Rotors (Post-processing step logic)
        stepRotorsChain();
        logDebug("  [STEP]  Rotors moved to next position: %s", getCurrentRotorPositions());

        // Convert char to index for rotor processing
        int currentIndex = keyboard.toIndex(input);
        logDebug("  [IN]    Rotor Input index: %d ('%c')", currentIndex, input);

        // Electrical Path
        currentIndex = passThroughRotorsForward(currentIndex);
        currentIndex = passThroughReflector(currentIndex);
        currentIndex = passThroughRotorsBackward(currentIndex);

        // Convert back to Char
        char outputChar = keyboard.toChar(currentIndex);
        logDebug("  [OUT]   Rotor output: %d ('%c')", currentIndex, outputChar);

        return outputChar;
    }

    // Steps the rotor chain: Index 0 is Rightmost and steps first
    private void stepRotorsChain() {
        if (activeRotors == null || activeRotors.isEmpty())
            return;

        boolean carry = true;
        for (Rotor activeRotor : activeRotors) {
            if (carry) {
                carry = activeRotor.step();
            } else {
                break;
            }
        }
    }

    private int passThroughRotorsForward(int index) {
        // Iterate from Right (0) to Left (Size-1)
        for (int i = 0; i < activeRotors.size(); i++) {
            Rotor rotor = activeRotors.get(i);
            int indexBefore = index;
            index = rotor.mapForward(index);

            String positionDesc = (i == 0) ? "Right" : (i == activeRotors.size() - 1) ? "Left " : "Mid  ";
            logDebug("  [FWD]   %s Rotor (ID %d): %d -> %d", positionDesc, rotor.getId(), indexBefore, index);
        }
        return index;
    }

    private int passThroughReflector(int index) {
        int indexBefore = index;
        index = activeReflector.getPairedIndex(index);

        logDebug("  [REF]   Reflector: %d -> %d", indexBefore, index);
        return index;
    }

    private int passThroughRotorsBackward(int index) {
        // Iterate from Left (Size-1) to Right (0)
        for (int i = activeRotors.size() - 1; i >= 0; i--) {
            Rotor rotor = activeRotors.get(i);
            int indexBefore = index;
            index = rotor.mapBackward(index);

            logDebug("  [BWD]   %s Rotor (ID %d): %d -> %d", i, rotor.getId(), indexBefore, index);
        }
        return index;
    }

    // Configures the machine with a specific set of rotors, starting positions, and a reflector
    @Override
    public void setConfiguration(List<Integer> rotorIDs, List<Character> startingPositions, String reflectorID) {
        this.activeReflector = allAvailableReflectors.get(reflectorID);
        if (this.activeReflector == null) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                 USER_REFLECTOR_NOT_FOUND, reflectorID );
        }
        //  Check if the number of selected rotors matches the XML definition
        if (rotorIDs.size() != this.rotorsCount) {
            throw new EnigmaException(
                    EnigmaException.ErrorCode.USER_INVALID_ROTOR_COUNT,
                    this.rotorsCount,
                    rotorIDs.size()
            );
        }
        // Configure Rotors (Right to Left), rotorIDs input is Left to Right (3, 2, 1).
        // We need to store them Right to Left for correct processing logic

        setupRotors(rotorIDs, startingPositions);
    }

    private void setupRotors(List<Integer> rotorIDs, List<Character> startingPositions) {
        this.activeRotors.clear();

        // Configure Rotors (Right to Left)
        for (int i = rotorIDs.size() - 1; i >= 0; i--) {
            int id = rotorIDs.get(i);
            Rotor rotor = allAvailableRotors.get(id);
            if (rotor == null) {
                throw new EnigmaException(
                        EnigmaException.ErrorCode.USER_ROTOR_NOT_FOUND,
                        id,
                        allAvailableRotors.keySet()
                );
            }
            char startChar = startingPositions.get(i);
            rotor.setPosition(keyboard.toIndex(startChar));

            this.activeRotors.add(rotor);

        }
    }

    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode; }

    private void logDebug(String format, Object... args) {

        if (debugMode) System.out.printf(format + "%n", args);
    }

    // Helper needed for specs
    public String formatConfiguration(List<Integer> rotorIDs, List<Character> positions, String reflectorID) {
        return formatter.formatConfiguration(rotorIDs, positions, reflectorID);
    }

    @Override
    public int getProcessedMessages() {
        return processedMessages;
    }

    @Override
    public List<Character> getCurrentRotorPositions() {
        List<Character> positions = new ArrayList<>();

        // Iterate backwards to display Left to Right
        for (int i = activeRotors.size() - 1; i >= 0; i--) {
            Rotor rotor = activeRotors.get(i);
            positions.add(this.keyboard.getABC().charAt(rotor.getPosition()));
        }
        return positions;
    }

    @Override
    public int getAllRotorsCount() { return allAvailableRotors.size(); }

    @Override
    public int getAllReflectorsCount() { return allAvailableReflectors.size(); }

    @Override
    public Map<Integer, Rotor> getAllAvailableRotors() { return allAvailableRotors; }

    @Override
    public Map<String, Reflector> getAllAvailableReflectors() { return allAvailableReflectors; }

    @Override
    public Keyboard getKeyboard() {
        return keyboard; }

    @Override
    public Plugboard getPlugboard() {
        return this.plugboard;
    }

    @Override
    public int getRotorsCount() {
        return this.rotorsCount;
    }
}