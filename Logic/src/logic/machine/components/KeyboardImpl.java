/**
 * Defines the Keyboard used by the Enigma machine.
 * Provides mapping between characters and their numeric indices,
 * ensuring all components use a consistent character set.
 */
package logic.machine.components;
import logic.exceptions.EnigmaException;
import java.util.*;
import java.util.stream.Collectors;
import java.io.Serializable;

/**
 * Defines the Keyboard used by the Enigma machine.
 * Provides mapping between characters and their numeric indices,
 * ensuring all components use a consistent character set.
 */
public class KeyboardImpl implements Keyboard, Serializable {
    private final List<Character> symbols;
    private final Map<Character, Integer> charToIndex;

    // Initializes the keyboard from a raw string
    public KeyboardImpl(String rawKeyboard) {
        if (rawKeyboard == null || rawKeyboard.trim().isEmpty()) {
            throw new EnigmaException(EnigmaException.ErrorCode.KEYBOARD_EMPTY);
        }

        // Create and Validate Symbols List
        this.symbols = createSymbolsList(rawKeyboard);
        validateForDuplicates(this.symbols);

        // Create final mapping
        this.charToIndex = createMapping(this.symbols);
    }

    private List<Character> createSymbolsList(String rawKeyboard) {
        // Removing whitespaces and converting to a continuous string
        String cleanString = rawKeyboard.chars()
                .mapToObj(c -> (char) c)
                .filter(c -> !Character.isWhitespace(c))
                .map(String::valueOf)
                .collect(Collectors.joining());

        // Convert clean string to list of characters
        return cleanString.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
    }

    private void validateForDuplicates(List<Character> symbols) {
        // Check for duplicates
        Set<Character> charSet = new HashSet<>(symbols);
        if (charSet.size() != symbols.size()) {
            throw new EnigmaException(EnigmaException.ErrorCode.KEYBOARD_DUPLICATE_SYMBOLS);
        }
    }

    private Map<Character, Integer> createMapping(List<Character> symbols) {
        Map<Character, Integer> mapping = new HashMap<>();
        int index = 0;
        for (char c : symbols) {
            mapping.put(c, index++);
        }
        return mapping;
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
                .orElseThrow(() -> new EnigmaException(EnigmaException.ErrorCode.
                        KEYBOARD_INVALID_CHAR,
                        c));

    }

    // Return the character of the given index
    @Override
    public char toChar(int index) {
        return Optional.of(index)
                .filter(i -> i >= 0 && i < symbols.size())
                .map(symbols::get)
                .orElseThrow(() -> new EnigmaException(EnigmaException.ErrorCode.
                        KEYBOARD_OUT_OF_RANGE,
                        index,symbols.size()-1
                        ));
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

    @Override
    public CharSequence getABC() {
        return asString();
    }
}
