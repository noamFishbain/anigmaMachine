package logic.machine.components;

/**
 * Keyboard contract used by the machine and its components.
 */
public interface Keyboard {

    // Returns number of symbols in this keyboard
    int size();

    // Converts a character into its index
    int toIndex(char c);

    // Converts an index into its character representation
    char toChar(int index);

    // Checks if a character exists in this keyboard
    boolean contains(char c);

    // Returns the entire keyboard as a continuous string
    String asString();

    CharSequence getABC();
}
