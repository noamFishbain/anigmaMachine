package logic.machine.components;

/**
 * Reflector contract.
 * Provides a symmetric mapping that sends the signal back through the rotors.
 */
public interface Reflector {

    // Returns total number of symbols handled by this reflector
    int getKeyboardSize();

    // Returns the paired index for the given index
    int getPairedIndex(int index);

    // Optional getters that might be useful to the machine
    int getId();
}
