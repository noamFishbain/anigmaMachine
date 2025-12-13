/**
 * Represents the Enigma reflector.
 * Provides a fixed, symmetric mapping that redirects the signal
 * back through the rotors in reverse. Has no moving parts.
 */
package logic.machine.components;

public class ReflectorImpl implements Reflector {

    // Reflection mapping: for each index i, reflectionMapping[i] = paired index
    private final int[] reflectionMapping;
    private int id;

    public ReflectorImpl(int[] reflectionMapping, int id) {
        validateMapping(reflectionMapping);     // ensure mapping is legal
        this.reflectionMapping = reflectionMapping.clone();
        this.id = id;
    }

    public ReflectorImpl(int[] reflectionMapping) {
        validateMapping(reflectionMapping);     // ensure mapping is legal
        this.reflectionMapping = reflectionMapping.clone();
    }

     // Return total number of symbols handled by the reflector
     @Override
     public int getKeyboardSize() {

        return reflectionMapping.length;
    }

    // Returns the paired index for the given index.
    @Override
    public int getPairedIndex(int index) {
        if (index < 0 || index >= reflectionMapping.length) {
            throw new IllegalArgumentException(
                    "Index out of range for reflector: " + index
            );
        }

        return reflectionMapping[index];
    }

    @Override
    public int getId() {

        return id;
    }

    // Factory method to create a basic reflector where:
    public static ReflectorImpl createBasicReflector(int keyboardSize) { // Before XML use
        validateSize(keyboardSize);

        int[] mapping = new int[keyboardSize];

        // Pair indexes
        for (int i = 0; i < keyboardSize; i += 2) {
            mapping[i] = i + 1;
            mapping[i + 1] = i;
        }

        return new ReflectorImpl(mapping);
    }

    // Validates the reflector mapping
    private void validateMapping(int[] mapping) {
        int mappingLength = mapping.length;

        // Reflector must have even size
        validateSize(mappingLength);

        // Validate basic constraints
        validateBasicConstraints(mapping, mappingLength);

        // Validate symmetry: if i -> j then j -> i
        validateSymmetry(mapping, mappingLength);
    }

    private void validateBasicConstraints(int[] mapping, int length) {
        for (int i = 0; i < length; i++) {
            int j = mapping[i];

            if (j < 0 || j >= length) {
                throw new IllegalArgumentException("Reflector mapping out of range: mapping[" + i + "] = " + j);
            }
            if (j == i) {
                throw new IllegalArgumentException("Reflector cannot map index to itself: " + i);
            }
        }
    }

    private void validateSymmetry(int[] mapping, int length) {
        for (int i = 0; i < length; i++) {
            int j = mapping[i];
            if (mapping[j] != i) {
                throw new IllegalArgumentException("Reflector mapping is not symmetric: " + i + " <-> " + j);
            }
        }
    }

    private static void validateSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Keyboard size must be positive.");
        }
        if (size % 2 != 0) {
            throw new IllegalArgumentException("Keyboard size must be even (required for pairing). Got: " + size);
        }
    }
}
