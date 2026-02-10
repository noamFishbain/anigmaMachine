package logic.machine.components;

/**
 * Represents the Plugboard component of the Enigma Machine
 * The plugboard allows for swapping pairs of letters before they enter the rotors
 * and after they return from the rotors
 */
public interface Plugboard {

    // Swaps the input character if it is connected to another character with plug.
    // If no plug is connected to the input character, the original character is returned
    char convert(char input);

    // Creates a bidirectional connection (a plug) between two characters
    void addPlug(char char1, char char2);

    // Removes all connections from the plugboard, resetting it to an empty state
    void clear();
}