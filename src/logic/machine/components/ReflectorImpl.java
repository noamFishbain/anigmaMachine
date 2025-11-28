/**
 * Represents the Enigma reflector.
 * Provides a fixed, symmetric mapping that redirects the signal
 * back through the rotors in reverse. Has no moving parts.
 */
package logic.machine.components;

public class ReflectorImpl implements Reflector {

    // Reflection mapping: for each index i, reflectionMapping[i] = paired index
    private final int[] reflectionMapping;

    public ReflectorImpl(int[] reflectionMapping) {
        validateMapping(reflectionMapping);     // ensure mapping is legal
        this.reflectionMapping = reflectionMapping.clone(); // keep internal copy
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

    // Factory method to create a basic reflector where:
    public static ReflectorImpl createBasicReflector(int keyboardSize) { // Before XML use
        if (keyboardSize <= 0) {
            throw new IllegalArgumentException(
                    "keyboard size must be positive."
            );
        }
        if (keyboardSize % 2 != 0) {
            throw new IllegalArgumentException(
                    "keyboard size must be even (required for pairing)."
            );
        }

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
        if (mappingLength % 2 != 0) {
            throw new IllegalArgumentException(
                    "Reflector size must be even. Got: " + mappingLength
            );
        }

        // Validate basic constraints
        for (int i = 0; i < mappingLength; i++) {
            int j = mapping[i];

            // The index must be within the valid range of the array
            if (j < 0 || j >= mappingLength) {
                throw new IllegalArgumentException(
                        "Reflector mapping out of range: mapping[" + i + "] = " + j
                );
            }

            // No self-mapping allowed
            if (j == i) {
                throw new IllegalArgumentException(
                        "Reflector cannot map index to itself: " + i
                );
            }
        }

        // Validate symmetry: if i -> j then j -> i
        for (int i = 0; i < mappingLength; i++) {
            int j = mapping[i];
            if (mapping[j] != i) {
                throw new IllegalArgumentException(
                        "Reflector mapping is not symmetric: " + i + " <-> " + j
                );
            }
        }
    }
}
