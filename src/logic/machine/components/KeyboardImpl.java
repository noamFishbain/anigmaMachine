/**
 * Defines the Keyboard used by the Enigma machine.
 * Provides mapping between characters and their numeric indices,
 * ensuring all components use a consistent character set.
 */
package logic.machine.components;

import java.util.*;
import java.util.stream.Collectors;

public class KeyboardImpl implements Keyboard {
    private final List<Character> symbols;
    private final Map<Character, Integer> charToIndex;

    public KeyboardImpl(String rawKeyboard) {
        if (rawKeyboard == null || rawKeyboard.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyboard cannot be null or empty");
        }

        // Removing whitespaces inside the Keyboard string (rawKeyboard)
        String clean = rawKeyboard.chars()
                .mapToObj(c -> (char) c)
                .filter(c -> !Character.isWhitespace(c))
                .map(String::valueOf)
                .collect(Collectors.joining());

        // Convert to list of characters
        this.symbols = clean.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        // Check for duplicates using set
        Set<Character> charSet = new HashSet<>(symbols);
        if (charSet.size() != symbols.size()) {
            throw new IllegalArgumentException("Keyboard contains invalid symbols");
        }

        // Mapping char to index
        this.charToIndex = new HashMap<>();
        int index = 0;
        for (char c : symbols) {
            charToIndex.put(c, index++);
        }
    }

    // Return the number of symbols in the Keyboard
    @Override
    public int size() {
        return symbols.size();
    }

    // Return the index of the given character
    @Override
    public int toIndex(char c) {
        return Optional.ofNullable(charToIndex.get(c))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Character '" + c + "' is not part of this Keyboard"));
    }

    // Return the character of the given index
    @Override
    public char toChar(int index) {
        return Optional.of(index)
                .filter(i -> i >= 0 && i < symbols.size())
                .map(symbols::get)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Index " + index + " is out of range (0.." + (symbols.size() - 1) + ")"));
    }

    // Checks whether a given character exists in the Keyboard
    @Override
    public boolean contains(char c) {
        return charToIndex.containsKey(c);
    }

    // Returns the entire Keyboard as a single continuous string
    @Override
    public String asString() {
        return symbols.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
