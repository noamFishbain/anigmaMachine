package logic.loader.validation;

import jaxb.schema.generated.BTEEnigma;
import jaxb.schema.generated.BTEPositioning;
import jaxb.schema.generated.BTEReflector;
import jaxb.schema.generated.BTERotor;
import logic.exceptions.EnigmaException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Utility class responsible for performing the logical validation rules for XML
public class XmlValidationRules {

    // Validates the logical integrity of the loaded XML data
    public void validateMachineSpecs(BTEEnigma enigma) throws Exception {
        String abc = enigma.getABC().trim();

        validateABC(abc);
        validateRotors(enigma.getBTERotors().getBTERotor(), abc); // Updated to pass ABC string
        validateReflectors(enigma.getBTEReflectors().getBTEReflector());
    }

    // Validation: alphabet length must be an even number
    private void validateABC(String abc) throws Exception {
        if (abc.length() % 2 != 0) {
            throw new EnigmaException(EnigmaException.ErrorCode.XML_ABC_ODD_LENGTH, abc.length());
        }
    }

    // Validates the loaded rotors against multiple rules: min count, sequential IDs, mapping size, and internal logic.
    private void validateRotors(List<BTERotor> rotors, String abc) throws Exception {
        int expectedMappingSize = abc.length();

        // Check if there are enough rotors defined (Minimum 3 expected)
        if (rotors.size() < 3) {
            throw new EnigmaException(EnigmaException.ErrorCode.XML_ROTOR_COUNT_LOW,3);
        }

        // Check sequential IDs (1, 2, 3...)
        List<Integer> ids = rotors.stream().map(BTERotor::getId).sorted().collect(Collectors.toList());
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) != (i + 1)) {
                throw new EnigmaException(EnigmaException.ErrorCode.XML_ROTOR_ID_SEQUENCE);
            }
        }

        // Check mapping size matches ABC and logical validity (no duplicates)
        for (BTERotor rotor : rotors) {
            if (rotor.getBTEPositioning().size() != expectedMappingSize) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        XML_ROTOR_MAPPING_SIZE,
                        rotor.getId());
            }

            // Validate mapping logic (Duplicate keys in Right/Left)
            validateRotorMappingLogic(rotor, abc);
        }
    }

    // Validates that a single rotor has a valid 1-to-1 mapping (bijective)
    private void validateRotorMappingLogic(BTERotor rotor, String abc) throws Exception {
        Set<String> rightSideChars = new HashSet<>();
        Set<String> leftSideChars = new HashSet<>();

        for (BTEPositioning pos : rotor.getBTEPositioning()) {
            String right = pos.getRight().toUpperCase();
            String left = pos.getLeft().toUpperCase();

            // Check if characters are valid (exist in ABC)
            if (abc.indexOf(right) == -1 || abc.indexOf(left) == -1) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        XML_ROTOR_INVALID_CHARS
                        , right, left);
            }

            // Check for duplicates in the RIGHT column (Source)
            // If "A" appears twice in 'right', it means 'A' maps to two different things to Invalid
            if (rightSideChars.contains(right)) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        XML_ROTOR_DUPLICATE_RIGHT
                        , rotor.getId(), right);
            }
            rightSideChars.add(right);

            // Check for duplicates in the LEFT column (Target)
            // In Enigma, rotors must be bijective (1-to-1), so duplicates here are also invalid
            if (leftSideChars.contains(left)) {
                throw new EnigmaException(EnigmaException.ErrorCode.
                        XML_ROTOR_DUPLICATE_LEFT,
                        rotor.getId(), left);
            }
            leftSideChars.add(left);
        }
    }

    // Validates the loaded reflectors: Checks for unique and sequential Roman IDs (I, II, III...).
    private void validateReflectors(List<BTEReflector> reflectors) throws Exception {
        // Check Sequential Roman IDs
        List<Integer> ids = reflectors.stream()
                .map(r -> convertRomanToInt(r.getId()))
                .sorted()
                .collect(Collectors.toList());

        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) != (i + 1)) {
                throw new EnigmaException(EnigmaException.ErrorCode.XML_REFLECTOR_ID_SEQUENCE);
            }
        }
    }

    // Converts Roman numeral strings (I-V) to their integer representation
    private int convertRomanToInt(String roman) {
        switch (roman.toUpperCase()) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            default: throw new EnigmaException(EnigmaException.ErrorCode.XML_REFLECTOR_UNKNOWN_ID,roman);
        }
    }
}