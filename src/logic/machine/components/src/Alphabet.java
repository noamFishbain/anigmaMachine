
/**
 * Defines the alphabet used by the Enigma machine.
 * Provides mapping between characters and their numeric indices,
 * ensuring all components use a consistent character set.
 */
package logic.machine.components.src;

import java.util.*;
import java.util.stream.Collectors;

public class Alphabet {
    private final List<Character> symbols;
    private final Map<Character, Integer> charToIndex;

    public Alphabet(String rawAlphabet) {
        if (rawAlphabet == null || rawAlphabet.trim().isEmpty()) {
            throw new IllegalArgumentException("Alphabet cannot be null or empty");
        }

        // removing whitespaces inside the alphabet string (rawAlphabet)
        String clean = rawAlphabet.chars()
                .mapToObj(c -> (char) c)
                .filter(c -> !Character.isWhitespace(c))
                .map(String::valueOf)
                .collect(Collectors.joining());

        //convert to list of characters
        this.symbols = clean.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());

        //check for duplicates using set
        Set<Character> charSet = new HashSet<>(symbols);
        if (charSet.stream() != symbols.size()) {
            throw new IllegalArgumentException("Alphabet contains invalid symbols");
        }

        //mapping char to index
        this.charToIndex = new HashMap<>();
        int index = 0;
        for (char c : symbols) {
            charToIndex.put(c, index++);
        }

        public int size () {
            return symbols.size();
        }

        // return the index of the given character
        public int toIndex ( char c){
            return Optional.ofNullable(charToIndex.get(c))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Character '" + c + "' is not part of this alphabet"));
        }

        //return the character of the given index
        public char toChar (inr index){
            return Optional.of(index)
                    .filter(index -> index >= 0 && index < symbols.size())
                    .map(symbols::get)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Index " + index + " is out of range (0.." + (symbols.size() - 1) + ")"));
        }
        public boolean contains(char c) {
            return charToIndex.containsKey(c);
        }
        public String asString() {
            return symbols.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining());
        }
    }
}
