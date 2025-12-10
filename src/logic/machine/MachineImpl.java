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

public class MachineImpl implements Machine {

    private int processedMessages = 0;
    private Keyboard keyboard;
    private List<Rotor> activeRotors; // List storing rotors. Index 0 = Rightmost (Fastest), Last Index = Leftmost (Slowest)
    private Reflector activeReflector;
    private Map<Integer, Rotor> allAvailableRotors;
    private Map<String, Reflector> allAvailableReflectors;
    private boolean debugMode = true;

    public MachineImpl() {
        initSimpleMachine();
    }

    public MachineImpl(MachineDescriptor descriptor) {
        this.keyboard = new KeyboardImpl(descriptor.getAlphabet());
        this.processedMessages = 0;
        this.allAvailableRotors = new HashMap<>();
        this.allAvailableReflectors = new HashMap<>();
        this.activeRotors = new ArrayList<>();
        this.activeReflector = null;

        // Load Rotors
        int keyboardSize = descriptor.getAlphabet().length();
        for (RotorDescriptor rotorDescriptor : descriptor.getRotors()) {
            int[] mapping = new int[keyboardSize];
            for (int i = 0; i < keyboardSize; i++) {
                mapping[i] = rotorDescriptor.getMapping().get(i);
            }
            Rotor rotor = new RotorImpl(
                    rotorDescriptor.getId(),
                    mapping,
                    rotorDescriptor.getNotchPosition() - 1, 0
            );
            this.allAvailableRotors.put(rotor.getId(), rotor);
        }

        // Load Reflectors
        for (ReflectorDescriptor reflectorDesc : descriptor.getReflectors()) {
            int[] mapping = new int[keyboardSize];
            Arrays.fill(mapping, -1);
            for (int[] pair : reflectorDesc.getPairs()) {
                int inputIndex = pair[0];
                int outputIndex = pair[1];
                mapping[inputIndex] = outputIndex;
                mapping[outputIndex] = inputIndex;
            }
            Reflector reflector = new ReflectorImpl(mapping);
            this.allAvailableReflectors.put(reflectorDesc.getId(), reflector);
        }
    }

    private void initSimpleMachine() {
        this.keyboard = new KeyboardImpl("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        int size = keyboard.size();
        Rotor leftRotor  = new RotorImpl(1, createShiftMapping(size, 1), size - 1, 0);
        Rotor middleRotor = new RotorImpl(2, createShiftMapping(size, 2), size - 1, 0);
        Rotor rightRotor  = new RotorImpl(3, createShiftMapping(size, 3), size - 1, 0);
        // Store as [Right, Middle, Left] to match processing logic
        this.activeRotors = new ArrayList<>(Arrays.asList(rightRotor, middleRotor, leftRotor));
        this.activeReflector = ReflectorImpl.createBasicReflector(size);
        this.processedMessages = 0;
    }

    private int[] createShiftMapping(int keyboardSize, int shift) {
        int[] mapping = new int[keyboardSize];
        for (int i = 0; i < keyboardSize; i++) {
            mapping[i] = (i + shift) % keyboardSize;
        }
        return mapping;
    }

    @Override
    public int getProcessedMessages() {
        return processedMessages;
    }

    @Override
    public List<Character> getCurrentRotorPositions() {
        List<Character> positions = new ArrayList<>();
        // Iterate backwards to display Left -> Right
        for (int i = activeRotors.size() - 1; i >= 0; i--) {
            Rotor rotor = activeRotors.get(i);
            positions.add(this.keyboard.getABC().charAt(rotor.getPosition()));
        }
        return positions;
    }

    @Override
    public MachineSpecs getMachineSpecs() {
        return null;
    }

    private void incrementProcessedMessages() {
        processedMessages++;
    }

    @Override
    public void configure() {}

    @Override
    public void setInitialCode() {
        resetToInitialCode();
    }

    @Override
    public void resetToInitialCode() {
        // Implementation for reset logic should go here using saved configuration
    }
    // Implement code initialization logic
    // For now the "initial code" is just the default positions (all zeros)
//    @Override
//    public void setInitialCode() {
//        resetToInitialCode();
//    }

    // Reset machine to initial configuration
//    @Override
//    public void resetToInitialCode() {
//        // For the simple version: just rebuild the hard-coded machine
//        initSimpleMachine();
//    }

    @Override
    public String process(String input) {
        incrementProcessedMessages();
        if (input == null || input.isEmpty()) return "";

        String normalized = input.toUpperCase();
        StringBuilder result = new StringBuilder();

        logDebug("--- [START] Processing String: %s ---", normalized);

        for (char c : normalized.toCharArray()) {
            if (!keyboard.contains(c)) {
                result.append(c);
                continue;
            }
            result.append(processSingleCharacter(c));
        }

        logDebug("--- [END] Process Completed. Result: %s ---\n", result.toString());
        return result.toString();
    }

    /**
     * Handles the complete flow of a single character through the machine.
     * Assumes activeRotors is stored as [Right, Middle, Left].
     */
    private char processSingleCharacter(char inputChar) {
        logDebug("\n[CHAR] Processing character: '%c'", inputChar);
        logDebug("  [STATE] Rotors BEFORE process (Left->Right): %s", getCurrentRotorPositions());

        // 4. Step Rotors (Post-processing step logic)
        stepRotorsChain();
        logDebug("  [STEP]  Rotors moved to next position: %s", getCurrentRotorPositions());

        int currentIndex = keyboard.toIndex(inputChar);
        logDebug("  [IN]    Input index: %d ('%c')", currentIndex, inputChar);

        // 1. Forward Path: Right -> Middle -> Left
        // Since index 0 is Rightmost, we iterate 0 -> Size
        for (int i = 0; i < activeRotors.size(); i++) {
            Rotor rotor = activeRotors.get(i);
            int indexBefore = currentIndex;
            currentIndex = rotor.mapForward(currentIndex);

            String positionDesc = (i == 0) ? "Right" : (i == activeRotors.size() - 1) ? "Left " : "Mid  ";
            logDebug("  [FWD]   %s Rotor (ID %d): %d -> %d", positionDesc, rotor.getId(), indexBefore, currentIndex);
        }

        // 2. Reflector
        int indexBeforeReflect = currentIndex;
        currentIndex = activeReflector.getPairedIndex(currentIndex);
        String reflectorId = (activeReflector instanceof ReflectorImpl) ? String.valueOf(((ReflectorImpl) activeReflector).getId()) : "N/A";
        logDebug("  [REF]   Reflector ID %s:      %d -> %d", reflectorId, indexBeforeReflect, currentIndex);

        // 3. Backward Path: Left -> Middle -> Right
        // We iterate Size -> 0
        for (int i = activeRotors.size() - 1; i >= 0; i--) {
            Rotor rotor = activeRotors.get(i);
            int indexBefore = currentIndex;
            currentIndex = rotor.mapBackward(currentIndex);

            String positionDesc = (i == 0) ? "Right" : (i == activeRotors.size() - 1) ? "Left " : "Mid  ";
            logDebug("  [BWD]   %s Rotor (ID %d): %d -> %d", positionDesc, rotor.getId(), indexBefore, currentIndex);
        }

        char outputChar = keyboard.toChar(currentIndex);
        logDebug("  [OUT]   Final output: %d ('%c')", currentIndex, outputChar);



        return outputChar;
    }

    // Steps the rotor chain: Index 0 is Rightmost and steps first
    private void stepRotorsChain() {
        if (activeRotors == null || activeRotors.isEmpty()) return;

        boolean carry = true;
        for (int i = 0; i < activeRotors.size(); i++) {
            if (carry) {
                carry = activeRotors.get(i).step();
            } else {
                break;
            }
        }
    }

    // Sets configuration ensuring activeRotors is [Right, Middle, Left]
    @Override
    public void setConfiguration(List<Integer> rotorIDs, List<Character> startingPositions, String reflectorID) {
        this.activeReflector = allAvailableReflectors.get(reflectorID);
        if (this.activeReflector == null) {
            throw new IllegalArgumentException("Reflector ID " + reflectorID + " is not available.");
        }

        List<Rotor> selectedRotors = new ArrayList<>();
        // rotorIDs input is usually Left->Right (e.g., 3, 2, 1).
        // We need to store them Right->Left (1, 2, 3) for correct processing logic.
        // So we iterate backwards through the input lists.
        for (int i = rotorIDs.size() - 1; i >= 0; i--) {
            int id = rotorIDs.get(i);
            Rotor rotor = allAvailableRotors.get(id);
            if (rotor == null) throw new IllegalArgumentException("Rotor ID " + id + " not found.");

            char startChar = startingPositions.get(i);
            ((RotorImpl) rotor).setPosition(keyboard.toIndex(startChar));

            selectedRotors.add(rotor);
        }
        this.activeRotors = selectedRotors;
    }

    // ... [Rest of the getters/setters/formatting methods remain the same] ...

    @Override
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }

    private void logDebug(String format, Object... args) {
        if (debugMode) System.out.printf(format + "%n", args);
    }

    @Override
    public List<Rotor> getActiveRotors() { return activeRotors; }

    @Override
    public Reflector getActiveReflector() { return activeReflector; }

    @Override
    public int getAllRotorsCount() { return allAvailableRotors.size(); }

    @Override
    public int getAllReflectorsCount() { return allAvailableReflectors.size(); }

    @Override
    public Map<Integer, Rotor> getAllAvailableRotors() { return allAvailableRotors; }

    @Override
    public Map<String, Reflector> getAllAvailableReflectors() { return allAvailableReflectors; }

    @Override
    public Keyboard getKeyboard() { return keyboard; }

    // Helper needed for specs
    public String formatConfiguration(List<Integer> rotorIDs, List<Character> positions, String reflectorID) {
        if (rotorIDs.size() != positions.size()) return "";
        StringBuilder sb = new StringBuilder();

        // IDs: Print Left to Right
        sb.append("<");
        for( int i = 0; i < rotorIDs.size(); i++) {
            sb.append(rotorIDs.get(i));
            if(i != rotorIDs.size() - 1) sb.append(", ");
        }
        sb.append(">");

        // Positions: Print Left to Right
        sb.append("<");
        for (int i = rotorIDs.size() - 1; i >= 0; i--) {
            int id = rotorIDs.get(i);
            char pos = positions.get(i);
            int notch = allAvailableRotors.get(id).getNotch();
            int dist = (notch - keyboard.toIndex(pos) + keyboard.size()) % keyboard.size();
            sb.append(pos).append("(").append(dist).append(")");
            if (i > 0) sb.append(",");
        }
        sb.append(">");

        sb.append("<").append(reflectorID).append(">");
        return sb.toString();
    }
}
        // -----------------------------------------------------------
        // 3. Reflector ID Part: <RomanID>
        // -----------------------------------------------------------
        sb.append("<");
        //sb.append(convertIntToRoman(reflectorID));
        sb.append(reflectorID);
        sb.append(">");

        return sb.toString();
    }

    // Helper to convert Int ID back to Roman (for display)
//    private String convertIntToRoman(String id) {
//        int idRoman = Integer.parseInt(id);
//        switch (idRoman) {
//            case 1: return "I";
//            case 2: return "II";
//            case 3: return "III";
//            case 4: return "IV";
//            case 5: return "V";
//            default: return id;
//        }
//    }

    @Override
    public int getAllRotorsCount() {
        return allAvailableRotors.size();
    }

    @Override
    public int getAllReflectorsCount() {
        return allAvailableReflectors.size();
    }


    @Override
    public Map<Integer, Rotor> getAllAvailableRotors() {
        return allAvailableRotors;
    }

    @Override
    public Map<String, Reflector> getAllAvailableReflectors() {
        return allAvailableReflectors;
    }

    @Override
    public Keyboard getKeyboard() {
        return keyboard;
    }
}

