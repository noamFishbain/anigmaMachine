package logic.machine.components;

/**
 * Represents a single Enigma rotor.
 * Contains the forward and backward wiring, notch position,
 * and current rotation offset. Supports stepping and character mapping.
 */
public class RotorImpl implements Rotor {
    private final int id;
    private int position; // The rotor's current rotational offset (changes every time the rotor steps)
    private final int notchPosition; // The notch position at which this rotor triggers the next rotor to advance
    private final int[] forwardMapping; // Forward wiring: maps input index to output index
    private final int[] backwardMapping; // Backward wiring: the inverse of forwardMapping
    private final int keyboardSize; // Total number of symbols the rotor supports

    public RotorImpl(int id, int[] forwardMapping, int notchPosition, int initialPosition) {
        validateInitialInput(forwardMapping);

        // Keep our own defensive copies
        this.id = id;
        this.keyboardSize = forwardMapping.length;

        validateMapping(forwardMapping, keyboardSize);
        this.forwardMapping = forwardMapping.clone();
        this.backwardMapping = buildBackwardMapping(this.forwardMapping, keyboardSize);

        this.notchPosition = validateAndSetNotch(notchPosition, keyboardSize);
        this.position = validateAndSetPosition(initialPosition, keyboardSize);
    }

    private void validateInitialInput(int[] forwardMapping) {
        if (forwardMapping == null || forwardMapping.length == 0) {
            throw new IllegalArgumentException("Rotor mapping cannot be null or empty");
        }
    }

    // Validate notch position
    private int validateAndSetNotch(int notchPosition, int size) {
        if (notchPosition < 0 || notchPosition >= size) {
            throw new IllegalArgumentException(
                    "Notch position out of range: " + notchPosition +
                            " (valid: 0.." + (size - 1) + ")"
            );
        }
        return notchPosition;
    }

    // Validate and normalize initial position
    private int validateAndSetPosition(int initialPosition, int size) {
        if (initialPosition < 0 || initialPosition >= size) {
            throw new IllegalArgumentException(
                    "Initial position out of range: " + initialPosition +
                            " (valid: 0.." + (size - 1) + ")"
            );
        }
        return initialPosition;
    }

    // Ensures the mapping is a valid permutation where each output index appears once
    private void validateMapping(int[] mapping, int size) {
        boolean[] seen = new boolean[size];

        for (int i = 0; i < size; i++) {
            int target = mapping[i];

            if (target < 0 || target >= size) {
                throw new IllegalArgumentException(
                        "Rotor mapping out of range: mapping[" + i + "] = " + target
                );
            }

            if (seen[target]) {
                throw new IllegalArgumentException(
                        "Rotor mapping is not a permutation. Value " + target +
                                " appears more than once."
                );
            }
            seen[target] = true;
        }
    }

    // Builds the inverse (backward) mapping of the given forward mapping
    private int[] buildBackwardMapping(int[] forward, int size) {
        int[] backward = new int[size];

        for (int i = 0; i < size; i++) {
            int j = forward[i];
            backward[j] = i;
        }
        return backward;
    }

    //  Advances the rotor by one position
    @Override
    public boolean step() {
        position = (position + 1) % keyboardSize;

        return (this.position == this.notchPosition);
    }

    // Maps an input index through the rotor in the forward direction
    @Override
    public int mapForward(int index) {
        // adjust for current position
        int shifted = (index + position) % keyboardSize;

        // pass through wiring
        int wired = forwardMapping[shifted];

        // revert the positional shift
        return (wired - position + keyboardSize) % keyboardSize;
    }

    // Maps an input index through the rotor in the backward direction
    @Override
    public int mapBackward(int index) {
        // adjust for current position
        int shifted = (index + position) % keyboardSize;

        // pass through inverse wiring
        int wired = backwardMapping[shifted];

        // revert the shift
        return (wired - position + keyboardSize) % keyboardSize;
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
            throw new IllegalArgumentException("Position out of range: " + newPosition);
        }
        this.position = newPosition;
    }

    @Override
    public int getNotch(){
        return notchPosition;
    }
}
