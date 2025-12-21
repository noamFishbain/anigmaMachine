package logic.engine.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import logic.exceptions.EnigmaException;
/**
 * Utility class responsible for parsing raw string inputs
 * into structured data types needed by the engine.
 */
public class InputParser {

    // Converts a comma-separated string of rotor IDs into a List of Integers
    public List<Integer> parseRotorIDs(String s) {
        if (s == null || s.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Split, trim, and parse to Integer
        List<Integer> ids = Arrays.stream(s.trim().split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        return ids;
    }

    // Converts a decimal reflector number (1-5) into its Roman numeral ID ("I"-"V").
    public String convertIntToRoman(int num) {
        if (num < 1 || num > 5) {
            throw new EnigmaException(EnigmaException.ErrorCode.USER_INVALID_REFLECTOR_INPUT);
        }
        String[] roman = {"I", "II", "III", "IV", "V"};
        return roman[num - 1];
    }
}