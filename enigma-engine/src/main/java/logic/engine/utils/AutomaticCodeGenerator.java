package logic.engine.utils;

import logic.engine.CodeConfiguration;
import logic.machine.Machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Helper class responsible for generating random valid code configurations.
 * It randomly selects rotors, initial positions, and a reflector from the available machine components.
 */
public class AutomaticCodeGenerator {

    // Generates a new random configuration based on the machine's available components
    public CodeConfiguration generate(Machine machine) {
        // Get the number of rotors required by the machine
        int count = machine.getRotorsCount();

        // Select random Rotor IDs
        List<Integer> selectedRotorIDs = selectRandomRotors(machine, count);

        // Select random starting positions for each rotor
        List<Character> selectedPositions = selectRandomPositions(machine, count);

        // Select a random Reflector ID
        String selectedReflectorID = selectRandomReflector(machine);

        // Select a random plugs
        String selectedPlugs = selectRandomPlugs(machine);

        // Return the configuration object (Empty plugs string as per default logic)
        return new CodeConfiguration(selectedRotorIDs, selectedPositions, selectedReflectorID, "");
    }

    // Shuffles all available rotor IDs and picks the first count IDs
    private List<Integer> selectRandomRotors(Machine machine, int count) {
        List<Integer> availableRotorIDs = new ArrayList<>(machine.getAllAvailableRotors().keySet());

        // Shuffle the list to ensure randomness
        Collections.shuffle(availableRotorIDs);

        // Return the sublist of the required size
        return availableRotorIDs.subList(0, count);
    }

    // Picks a random character from the machine's alphabet for each required rotor
    private List<Character> selectRandomPositions(Machine machine, int count) {
        String keyboard = machine.getKeyboard().asString();
        Random random = new Random();
        List<Character> positions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Pick a random index from the alphabet string
            positions.add(keyboard.charAt(random.nextInt(keyboard.length())));
        }
        return positions;
    }

    // Picks a random reflector ID from the list of available reflectors
    private String selectRandomReflector(Machine machine) {
        List<String> availableReflectors = new ArrayList<>(machine.getAllAvailableReflectors().keySet());
        Random random = new Random();

        // Pick a random reflector from the list
        return availableReflectors.get(random.nextInt(availableReflectors.size()));
    }

    // Randomly generates a string of plugboard connections
    private String selectRandomPlugs(Machine machine) {
        String alphabet = machine.getKeyboard().asString();
        List<Character> chars = new ArrayList<>();
        for (char c : alphabet.toCharArray()) {
            chars.add(c);
        }

        // Shuffle the characters to ensure random pairing order
        Collections.shuffle(chars);

        Random random = new Random();

        // Calculate maximum possible pairs
        int maxPairs = chars.size() / 2;

        // Randomly determine how many pairs to use (from 0 to maxPairs inclusive)
        // If random.nextInt returns 0, it means NO plugs will be connected
        int numberOfPairs = random.nextInt(maxPairs + 1);

        StringBuilder plugs = new StringBuilder();
        for (int i = 0; i < numberOfPairs; i++) {
            // Since the list is shuffled and unique, picking sequentially guarantees unique pairs
            char c1 = chars.get(i * 2);
            char c2 = chars.get(i * 2 + 1);
            plugs.append(c1).append(c2);
        }

        return plugs.toString();
    }
}