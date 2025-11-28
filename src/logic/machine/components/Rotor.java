package logic.machine.components;

/**
 * Rotor contract.
 * Exposes the operations the machine needs from any rotor implementation.
 */
public interface Rotor {

    // Advance rotor by one position, and return true if notch is hit
    boolean step();

    // Map index from right to left (forward direction)
    int mapForward(int index);

    // Map index from left to right (backward direction)
    int mapBackward(int index);

    // Optional getters that might be useful to the machine
    int getId();

    // Returns current rotational position of the rotor
    int getPosition();

    // Returns total size of the alphabet used by the rotor
    int getAlphabetSize();
}
