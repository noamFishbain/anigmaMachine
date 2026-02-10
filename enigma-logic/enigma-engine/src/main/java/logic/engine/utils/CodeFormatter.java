package logic.engine.utils;

import logic.engine.CodeConfiguration;
import logic.machine.Machine;

/**
 * Utility class responsible for formatting the machine's configuration
 * into a specific string format required for UI display.
 * Example format: <RotorIDs><Positions><ReflectorID><Plugs>
 * e.g., <1,2><A(5),B(2)><I><A|B,C|D>
 */
public class CodeFormatter {

    // Generates the fully formatted string of the current or original code configuration.
    public static String formatCode(Machine machine, CodeConfiguration config) {
        if (config == null) {
            return "";
        }

        // Delegate basic formatting (Rotors, Notch distances, Reflector) to the Machine logic
        // This returns the part: <ID,ID><Pos(dist),Pos(dist)><ReflectorID>
        String baseOutput = machine.formatConfiguration(
                config.getRotorIdsInOrder(),
                config.getRotorPositions(),
                config.getReflectorId()
        );

        // Manually append the plugboard connections if they exist
        // This adds the part: <A|B,C|D>
        String plugs = config.getPlugs();
        if (plugs != null && !plugs.isEmpty()) {
            baseOutput += formatPlugsForDisplay(plugs);
        }

        return baseOutput;
    }

    // Helper method to format the raw plug string (e.g., "ABCD")
    private static String formatPlugsForDisplay(String plugs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");

        for (int i = 0; i < plugs.length(); i += 2) {
            // First char of pair
            sb.append(plugs.charAt(i));
            // Separator
            sb.append("|");
            // Second char of pair
            sb.append(plugs.charAt(i+1));

            // Add comma separator between pairs, but not after the last one
            if (i < plugs.length() - 2) {
                sb.append(",");
            }
        }
        sb.append(">");
        return sb.toString();
    }
}