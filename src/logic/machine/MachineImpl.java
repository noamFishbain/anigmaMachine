package logic.machine;

import logic.engine.MachineSpecs;
import logic.loader.dto.MachineDescriptor;
import logic.loader.dto.ReflectorDescriptor;
import logic.loader.dto.RotorDescriptor;
import logic.machine.components.Keyboard;
import logic.machine.components.KeyboardImpl;
import logic.machine.components.Rotor;
import logic.machine.components.RotorImpl;
import logic.machine.components.Reflector;
import logic.machine.components.ReflectorImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static jdk.jfr.internal.management.ManagementSupport.logDebug;

/**
 * Represents the complete Enigma machine.
 * Coordinates the flow of characters through the rotors, reflector,
 * and plugboard (when implemented).
 * Handles stepping logic and provides the main encryption interface.
 */
public class MachineImpl implements Machine {

    private int processedMessages = 0; // Total number of processed messages
    private Keyboard keyboard; // Keyboard used to map characters to indices and back
    private List<Rotor> rotors; // Ordered list of rotors
    private Reflector reflector; // Active reflector used by this machine

    // Temporary constructor that builds a simple hard-coded Enigma machine
    public MachineImpl() {
        initSimpleMachine();
    }

    /**
     * Constructor used by the engine.
     * For now, it delegates to the default constructor and ignores the descriptor.
     * In the next phase, this will build the machine based on the descriptor content.
     *
     * @param descriptor machine structure loaded from XML
     */
    public MachineImpl(MachineDescriptor descriptor) {
        //this();
    this.keyboard = new KeyboardImpl(descriptor.getAlphabet());
        this.processedMessages = 0;
        this.rotors = new ArrayList<>();
        for(RotorDescriptor rotorDescriptor : descriptor.getRotors()) {
            int [] mapping = new int[rotorDescriptor.getMapping().size()];
            for(int i = 0; i < rotorDescriptor.getMapping().size(); i++) {
                mapping[i] = rotorDescriptor.getMapping().get(i);
            }

        Rotor rotor = new RotorImpl(
                rotorDescriptor.getId(),
                mapping,
                rotorDescriptor.getNotchPosition(),
                0
        );
            this.rotors.add(rotor);
        }
// --- Reflector Initialization Section ---

// 1. Determine the required array size (must match the total ABC length)
        int alphabetSize = descriptor.getAlphabet().length();

        this.reflector = null; // Initialize

// 2. Iterate over all reflectors defined in the Descriptor
        for (ReflectorDescriptor reflectorDesc : descriptor.getReflectors()) {

            // Create a new mapping array for this reflector
            int[] mapping = new int[alphabetSize];

            // (Optional) Initialize with -1 to help detect bugs if a char is left unmapped
            Arrays.fill(mapping, -1);

            // 3. Fill the mapping array based on the pairs
            // Note: Reflector mapping is symmetric. If A maps to B, then B must map to A.
            for (int[] pair : reflectorDesc.getPairs()) {
                int inputIndex = pair[0];
                int outputIndex = pair[1];

                // Map both directions
                mapping[inputIndex] = outputIndex;
                mapping[outputIndex] = inputIndex;
            }

            // 4. Create the Reflector object and select the active one
            // Logic: We pick the first reflector we find, but if we find "I", we override and use it.
            if (this.reflector == null || reflectorDesc.getId().equals("I")) {
                this.reflector = new ReflectorImpl(mapping);

                // If we found "I", we can stop searching (priority given to I for this exercise)
                if (reflectorDesc.getId().equals("I")) {
                    break;
                }
            }
        }

// 5. Final validation to ensure a reflector was successfully created
        if (this.reflector == null) {
            throw new RuntimeException("Error: No reflector could be initialized from the descriptor.");
        }
    }


    // Temporary constructor that builds a simple hard-coded Enigma machine
    private void initSimpleMachine() {
        // Build a very simple keyboard
        this.keyboard = new KeyboardImpl("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int size = keyboard.size();

        // Build a few simple rotors with shift-based permutations
        Rotor leftRotor  = new RotorImpl(1, createShiftMapping(size, 1), size - 1, 0);
        Rotor middleRotor = new RotorImpl(2, createShiftMapping(size, 2), size - 1, 0);
        Rotor rightRotor  = new RotorImpl(3, createShiftMapping(size, 3), size - 1, 0);

        // Left → Middle → Right (signal will go right-to-left forward, then left-to-right backward)
        this.rotors = Arrays.asList(leftRotor, middleRotor, rightRotor);

        // Simple reflector that just pairs indices
        this.reflector = ReflectorImpl.createBasicReflector(size);

        // For now, we only initialize the message counter.
        this.processedMessages = 0;
    }

    // Builds a simple shift based permutation mapping
    private int[] createShiftMapping(int keyboardSize, int shift) {
        int[] mapping = new int[keyboardSize];
        for (int i = 0; i < keyboardSize; i++) {
            mapping[i] = (i + shift) % keyboardSize;
        }
        return mapping;
    }

    // Returns the number of messages processed so far
    @Override
    public int getProcessedMessages() {

        return processedMessages;
    }

    @Override
    public List<Character> getCurrentRotorPositions() {
        List<Character> positions = new ArrayList<>();
        for (Rotor rotor : this.rotors) {
            int currentIndex = rotor.getPosition();

            char currentLetter = this.keyboard.getABC().charAt(currentIndex);
            positions.add(currentLetter);
        }
        return positions;
    }

    // Internal helper: increments the message counter. Called automatically inside the process() method
    private void incrementProcessedMessages() {

        processedMessages++;
    }

    // Implement machine setup logic using descriptor data
    @Override
    public void configure() {

    }

    // Implement code initialization logic
    // For now the "initial code" is just the default positions (all zeros)
    @Override
    public void setInitialCode() {
        resetToInitialCode();
    }

    // Reset machine to initial configuration
    @Override
    public void resetToInitialCode() {
        // For the simple version: just rebuild the hard-coded machine
        initSimpleMachine();
    }

    // Processes the given text through the machine
//    @Override
//    public String process(String input) {
//        incrementProcessedMessages();
//
//        if (input == null || input.isEmpty()) {
//            return input;
//        }
//
//        // We will work in upper-case only for the simple keyboard
//        String normalized = input.toUpperCase();
//        StringBuilder result = new StringBuilder();
//
//        for (char c : input.toCharArray()) {
//
//            // Skip encryption for characters not in the keyboard
//            if (!keyboard.contains(c)) {
//                result.append(c);
//                continue;
//            }
//
//            // Step rotors before processing each character
//            stepRotorsChain();
//
//            // Char to index
//            int index = keyboard.toIndex(c);
//
//            // Forward through rotors: right to left
//            for (int i = rotors.size() - 1; i >= 0; i--) {
//                index = rotors.get(i).mapForward(index);
//            }
//
//            // Reflector
//            index = reflector.getPairedIndex(index);
//
//            // Backward through rotors: left to right
//            for (int i = 0; i < rotors.size(); i++) {
//                index = rotors.get(i).mapBackward(index);
//            }
//
//            // Index to char
//            char encoded = keyboard.toChar(index);
//            result.append(encoded);
//        }
//
//        return result.toString();
//    }

    @Override
    public String process(String input) {
        incrementProcessedMessages();

        if (input == null || input.isEmpty()) return "";

        String normalized = input.toUpperCase();
        StringBuilder result = new StringBuilder();

        logDebug("--- [START] Processing String: %s ---", normalized);

        for (char c : normalized.toCharArray()) {
            // If char is not in ABC, append as is and skip
            if (!keyboard.contains(c)) {
                result.append(c);
                continue;
            }

            // Delegate the heavy lifting to a helper method
            char processedChar = processSingleCharacter(c);
            result.append(processedChar);
        }

        logDebug("--- [END] Process Completed. Result: %s ---\n", result.toString());
        return result.toString();
    }
    /**
     * Handles the complete flow of a single character through the machine:
     * 1. Steps the rotors.
     * 2. Feeds the character through the forward path.
     * 3. Reflects.
     * 4. Feeds through the backward path.
     * 5. Logs everything if debug mode is on.
     */
    private char processSingleCharacter(char inputChar) {
        logDebug("\n[CHAR] Processing character: '%c'", inputChar);

        // --- Step 1: Rotate Rotors ---
        // Log state BEFORE rotation
        logDebug("  [STEP] Rotors BEFORE step: %s", getCurrentRotorPositions());

        stepRotorsChain(); // The actual mechanical movement

        // Log state AFTER rotation
        logDebug("  [STEP] Rotors AFTER step:  %s", getCurrentRotorPositions());


        // --- Step 2: Input Conversion ---
        int currentIndex = keyboard.toIndex(inputChar);
        logDebug("  [IN]   Input index: %d ('%c')", currentIndex, inputChar);


        // --- Step 3: Forward Path (Right to Left) ---
        // Iterating backwards because index 0 is the Rightmost rotor in our list
        for (int i = rotors.size() - 1; i >= 0; i--) {
            Rotor rotor = rotors.get(i);
            int indexBefore = currentIndex;

            currentIndex = rotor.mapForward(currentIndex);

            logDebug("  [FWD]  Rotor ID %d: %d -> %d", rotor.getId(), indexBefore, currentIndex);
        }


        // --- Step 4: Reflector ---
        int indexBeforeReflect = currentIndex;
        currentIndex = reflector.getPairedIndex(currentIndex); // Or .map(currentIndex)
        logDebug("  [REF]  Reflector ID %d: %d -> %d", reflector.getId(), indexBeforeReflect, currentIndex);


        // --- Step 5: Backward Path (Left to Right) ---
        for (int i = 0; i < rotors.size(); i++) {
            Rotor rotor = rotors.get(i);
            int indexBefore = currentIndex;

            currentIndex = rotor.mapBackward(currentIndex);

            logDebug("  [BWD]  Rotor ID %d: %d -> %d", rotor.getId(), indexBefore, currentIndex);
        }


        // --- Step 6: Final Result ---
        char outputChar = keyboard.toChar(currentIndex);
        logDebug("  [OUT]  Final output: %d ('%c')", currentIndex, outputChar);

        return outputChar;
    }


    // Steps the rotor chain according to a simple Enigma stepping scheme
    // If a rotor hits its notch, step the rotor to its left
    private void stepRotorsChain() {
        boolean carry = true;

        // Start from the right most rotor (last in the list)
        for (int i = rotors.size() - 1; i >= 0 && carry; i--) {
            carry = rotors.get(i).step();
        }
    }

    // Returns the machine specifications for display
    @Override
    public MachineSpecs getSpecs() {
        return new MachineSpecs(
                rotors.size(),
                1,                 // Right now only 1 reflector
                processedMessages,
                null,              // original code (TODO)
                null               // current code (TODO)
        );
    }
// ----------------- Debug Infrastructure -----------------

    private boolean debugMode = true;

    /**
     * Call this from Main/UI to enable verbose logging
     */
    @Override
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Internal helper to print logs only when debug is ON
     */
    private void logDebug(String format, Object... args) {
        if (debugMode) {
            // מדפיס בפורמט מסודר עם ירידת שורה
            System.out.printf(format + "%n", args);
        }
    }

}

