package utils;

public class ConfigurationUtils {

    // Converts a Roman numeral string (I-V) to its corresponding integer.
    public static int decodeRoman(String roman) {
        if (roman == null)
            return 1;
        return switch (roman.toUpperCase()) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            default -> 1;
        };
    }
}
