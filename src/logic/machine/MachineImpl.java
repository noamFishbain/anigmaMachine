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
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Represents the complete Enigma machine.
 * Coordinates the flow of characters through the rotors, reflector,
 * and plugboard (when implemented).
 * Handles stepping logic and provides the main encryption interface.
 */
public class MachineImpl implements Machine {

    private int processedMessages = 0; // Total number of processed messages
    private Keyboard keyboard; // Keyboard used to map characters to indices and back
    private List<Rotor> activeRotors; // Ordered list of rotors
    private Reflector activeReflector; // Active reflector used by this machine
    private Map<Integer, Rotor> allAvailableRotors; // Map that holds all the valid rotors from the XML
    private Map<String, Reflector> allAvailableReflectors; // Map that holds all the valid reflectors from the XML

    // Temporary constructor that builds a simple hard-coded Enigma machine
    public MachineImpl() {
        initSimpleMachine();
    }

    /**
     * Constructor used by the engine. Builds the comprehensive map of all available
     * rotors and reflectors from the descriptor, preparing the machine for configuration.
     * The machine starts in an unconfigured state.
     *
     * @param descriptor machine structure loaded from XML
     */
    public MachineImpl(MachineDescriptor descriptor) {
        //this();
        this.keyboard = new KeyboardImpl(descriptor.getAlphabet());
        this.processedMessages = 0;
        this.allAvailableRotors = new HashMap<>();
        this.allAvailableReflectors = new HashMap<>();
        this.activeRotors = null;
        this.activeReflector = null;

        // Build All Available Rotors and populate map
        int keyboardSize = descriptor.getAlphabet().length();
        for (RotorDescriptor rotorDescriptor : descriptor.getRotors()) {

            int[] mapping = new int[keyboardSize];
            for (int i = 0; i < keyboardSize; i++) {
                // Assuming mapping is index-based in RotorDescriptor (List<Integer>)
                mapping[i] = rotorDescriptor.getMapping().get(i);
            }

            Rotor rotor = new RotorImpl(
                    rotorDescriptor.getId(),
                    mapping,
                    rotorDescriptor.getNotchPosition() - 1, 0 // Starting position is always 0, until set by code
            );
            this.allAvailableRotors.put(rotor.getId(), rotor);
        }

        // Build All Available Reflectors and populate map
        for (ReflectorDescriptor reflectorDesc : descriptor.getReflectors()) {

            int[] mapping = new int[keyboardSize];
            Arrays.fill(mapping, -1);

            for (int[] pair : reflectorDesc.getPairs()) {
                int inputIndex = pair[0];
                int outputIndex = pair[1];

                // Map both directions (Reflector is symmetric)
                mapping[inputIndex] = outputIndex;
                mapping[outputIndex] = inputIndex;
            }

            Reflector reflector = new ReflectorImpl(mapping);
            this.allAvailableReflectors.put(reflectorDesc.getId(), reflector);
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
        this.activeRotors = Arrays.asList(leftRotor, middleRotor, rightRotor);

        // Simple reflector that just pairs indices
        this.activeReflector = ReflectorImpl.createBasicReflector(size);

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
        for (Rotor rotor : this.activeRotors) {
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
    @Override
    public String process(String input) {
        incrementProcessedMessages();

        if (input == null || input.isEmpty()) {
            return input;
        }

        // We will work in upper-case only for the simple keyboard
        String normalized = input.toUpperCase();
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {

            // Skip encryption for characters not in the keyboard
            if (!keyboard.contains(c)) {
                result.append(c);
                continue;
            }

            // Step rotors before processing each character
            stepRotorsChain();

            // Char to index
            int index = keyboard.toIndex(c);

            // Forward through rotors: right to left
            for (int i = activeRotors.size() - 1; i >= 0; i--) {
                index = activeRotors.get(i).mapForward(index);
            }

            // Reflector
            index = activeReflector.getPairedIndex(index);

            // Backward through rotors: left to right
            for (int i = 0; i < activeRotors.size(); i++) {
                index = activeRotors.get(i).mapBackward(index);
            }

            // Index to char
            char encoded = keyboard.toChar(index);
            result.append(encoded);
        }

        return result.toString();
    }

    // Steps the rotor chain according to a simple Enigma stepping scheme
    // If a rotor hits its notch, step the rotor to its left
    private void stepRotorsChain() {
        boolean carry = true;

        // Start from the right most rotor (last in the list)
        for (int i = activeRotors.size() - 1; i >= 0 && carry; i--) {
            carry = activeRotors.get(i).step();
        }
    }

    // Returns the machine specifications for display
    @Override
    public MachineSpecs getSpecs() {
        return new MachineSpecs(
                activeRotors.size(),
                1,                 // Right now only 1 reflector
                processedMessages,
                null,              // original code (TODO)
                null               // current code (TODO)
        );
    }

     // Sets the active components and their initial positions based on a code configuration
    @Override
    public void setConfiguration(List<Integer> rotorIDs, List<Character> startingPositions, String reflectorID) {

        // Set Active Reflector
        this.activeReflector = allAvailableReflectors.get(reflectorID);

        if (this.activeReflector == null) {
            // This should be caught by the engine validation layer, but good for safety
            throw new IllegalArgumentException("Reflector ID " + reflectorID + " is not available.");
        }

        // Set Active Rotors
        List<Rotor> selectedRotors = new ArrayList<>();

        for (int id : rotorIDs) {
            Rotor rotor = allAvailableRotors.get(id);
            if (rotor == null) {
                // This should also be caught by the engine validation layer
                throw new IllegalArgumentException("Rotor ID " + id + " is not available.");
            }

            selectedRotors.add(rotor);
        }

        this.activeRotors = selectedRotors;

        // Set Initial Positions
        for (int i = 0; i < activeRotors.size(); i++) {
            Rotor rotor = activeRotors.get(i);
            char startingChar = startingPositions.get(i);

            // Convert the character to its 0-based index
            int startingIndex = keyboard.toIndex(startingChar);

            ((RotorImpl) rotor).setPosition(startingIndex);
        }
    }
}
