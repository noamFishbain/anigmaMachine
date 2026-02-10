package logic.machine.components;

import logic.exceptions.EnigmaException;

import java.io.Serializable;
/**
 * Represents a single Enigma rotor.
 * Updated to use a Position-Based Mapping (Lookup Table) instead of simple index arrays.
 * This ensures the logic holds true regardless of the XML row order.
 */
public class RotorImpl implements Rotor , Serializable {
    private final int id;
    private int position; // The rotor's current rotational offset
    private final int notchPosition; // The notch position to trigger the next rotor

    // The normalized location table: [ABC Size][2]
    // index = The Character Index (A=0, B=1...)
    // col[0] = Row index in the RIGHT column
    // col[1] = Row index in the LEFT column
    private final int[][] letterPositions;

    private final int keyboardSize; // Total alphabet size

    // Constructor updated to accept int[][] mapping
    public RotorImpl(int id, int[][] letterPositions, int notchPosition, int initialPosition) {
        if (letterPositions == null || letterPositions.length == 0) {
            throw new EnigmaException(EnigmaException.ErrorCode.ROTOR_MAPPING_MISSING);
        }

        // Keep our own defensive copies
        this.id = id;
        this.keyboardSize = letterPositions.length;

        // Save the mapping table directly
        this.letterPositions = letterPositions;

        this.notchPosition = validateAndSetNotch(notchPosition, keyboardSize);
        this.position = validateAndSetPosition(initialPosition, keyboardSize);
    }

    // Validate notch position
    private int validateAndSetNotch(int notchPosition, int size) {
        if (notchPosition < 0 || notchPosition >= size) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    ROTOR_NOTCH_OUT_OF_RANGE,
                    notchPosition,size-1);
        }
        return notchPosition;
    }

    private  int validateAndSetPosition(int position, int size) {
        if (position < 0 || position >= size) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    ROTOR_POSITION_OUT_OF_RANGE,
                    position,size-1);
        }
        return position;
    }

    // Advances the rotor by one position
    @Override
    public boolean step() {
        position = (position + 1) % keyboardSize;

        return (this.position == this.notchPosition);
    }

    // Maps an input index through the rotor in the forward direction (Right to Left)
    @Override
    public int mapForward(int inputIndex) {
        // Calculate physical contact point on the Right side
        int contactIndex = (inputIndex + position) % keyboardSize;

        // Find which character connects to this Right contact index
        int outputLeftIndex = -1;

        for (int charId = 0; charId < keyboardSize; charId++) {
            // Check column 0 (Right Position)
            if (letterPositions[charId][0] == contactIndex) {
                // Found the character! Get its Left Position (column 1)
                outputLeftIndex = letterPositions[charId][1];
                break;
            }
        }

        if (outputLeftIndex == -1) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    ROTOR_BROKEN_MAPPING,
                    id);
        }

        // Calculate relative output index (Exit position - Offset)
        return (outputLeftIndex - position + keyboardSize) % keyboardSize;
    }

    // Maps an input index through the rotor in the backward direction (Left to Right)
    @Override
    public int mapBackward(int inputIndex) {
        // Calculate physical contact point on the Left side
        int contactIndex = (inputIndex + position) % keyboardSize;

        // Find which character connects to this Left contact index
        int outputRightIndex = -1;

        for (int charId = 0; charId < keyboardSize; charId++) {
            // Check column 1 (Left Position)
            if (letterPositions[charId][1] == contactIndex) {
                // Found the character! Get its Right Position (column 0)
                outputRightIndex = letterPositions[charId][0];
                break;
            }
        }

        if (outputRightIndex == -1) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    ROTOR_BROKEN_MAPPING,
                    id);
        }

        // Calculate relative output index (Exit position - Offset)
        return (outputRightIndex - position + keyboardSize) % keyboardSize;
    }

    @Override
    public int getId() {

        return id;
    }

    @Override
    public int getPosition() {

        return position;
    }

    @Override
    public int getKeyboardSize() {

        return keyboardSize;
    }

    @Override
    public void setPosition(int newPosition) {
        if (newPosition < 0 || newPosition >= keyboardSize) {
            throw new EnigmaException(EnigmaException.ErrorCode.
                    ROTOR_POSITION_OUT_OF_RANGE,
                    newPosition,keyboardSize-1);
        }

            // Check column 0 (Right Position)
            this.position = letterPositions[newPosition][0];
    }

    @Override
    public int getNotch(){
        return notchPosition;
    }
}