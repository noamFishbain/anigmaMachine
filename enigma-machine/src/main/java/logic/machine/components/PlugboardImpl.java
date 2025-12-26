package logic.machine.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PlugboardImpl implements Plugboard, Serializable {
    // Maps a character to its swapped partner (e.g: A to Z, Z to A)
    private final Map<Character, Character> plugs;

    public PlugboardImpl() {
        this.plugs = new HashMap<>();
    }

    @Override
    public char convert(char input) {
        // Return the mapped character if it exists, otherwise return the input itself
        return plugs.getOrDefault(input, input);
    }

    @Override
    public void addPlug(char char1, char char2) {
        validatePlug(char1, char2);

        // Create bidirectional connection
        plugs.put(char1, char2);
        plugs.put(char2, char1);
    }

    private void validatePlug(char c1, char c2) {
        // Cannot plug a letter to itself
        if (c1 == c2) {
            throw new IllegalArgumentException("Invalid plug: Cannot connect character '" + c1 + "' to itself.");
        }

        // Check if one of the characters is already plugged
        if (plugs.containsKey(c1) || plugs.containsKey(c2)) {
            throw new IllegalArgumentException("Invalid plug: Character '" + c1 + "' or '" + c2 + "' is already plugged.");
        }
    }

    @Override
    public void clear() {
        plugs.clear();
    }
}