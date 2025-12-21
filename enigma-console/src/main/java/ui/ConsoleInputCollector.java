package ui;

import logic.exceptions.EnigmaException;

import java.util.Scanner;

/**
 * Responsible for collecting and validating raw user input
 * for the Enigma code configuration (Rotors, Positions, Reflector ID).
 */
public class ConsoleInputCollector {

    private final Scanner scanner;

    public ConsoleInputCollector(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readValidRotorIDs(int requiredCount) {
        while (true) {
            System.out.printf("Enter %d Rotor IDs (Left to Right, comma separated): %n", requiredCount);
            // We assume user enters Left to Right. The Engine reverses it
            String input = ConsoleInputReader.readLine(scanner).trim();

            if (input.isEmpty()) {
                System.out.println(EnigmaException.ErrorCode.USER_INPUT_EMPTY);
                continue;
            }

            if (!input.matches("^[0-9, ]+$")) {
                System.out.println(EnigmaException.ErrorCode.USER_INPUT_NOT_NUMBER);
                continue;
            }

            return input;
        }
    }

    public String readValidPositions(int expectedLength) {
        while (true) {
            System.out.println("Enter Initial Positions (English letters only): ");
            String input = ConsoleInputReader.readLine(scanner).trim().toUpperCase();

        try {
            // Check 1: Length Validation using centralized Exception
            if (input.length() != expectedLength) {
                throw new EnigmaException(
                        EnigmaException.ErrorCode.USER_POSITION_COUNT_MISMATCH,
                        expectedLength,
                        input.length()
                );
            }

            // Check 2: Character Validation
            // We assume basic A-Z for initial input before validating against the machine's specific keyboard
            if (!input.matches("^[A-Z]+$")) {
                // Find the first invalid char for the error message
                for(char c : input.toCharArray()) {
                    if(c < 'A' || c > 'Z') {
                        throw new EnigmaException(EnigmaException.ErrorCode.INPUT_INVALID_CHARACTER, c, "A-Z");
                    }
                }
            }

            return input;

        } catch (EnigmaException e) {
            // Catch the centralized exception and display the formatted message
            System.out.println(e.getMessage());
        }
        }
    }

    public int readValidReflectorID() {
        while (true) {
            System.out.println("Enter Reflector ID (1=I, 2=II, 3=III, 4=IV, 5=V): ");
            int input = ConsoleInputReader.readInt(scanner);
            try {
                if (input < 1 || input > 5) {
                    throw new EnigmaException(EnigmaException.ErrorCode.USER_INVALID_REFLECTOR_INPUT);
                }
                return input;
            }
            catch (EnigmaException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Reads plugboard settings from the user
    public String readValidPlugs() {
        while (true) {
            System.out.println("Enter plugs as a continuous string (e.g. AB49) or press Enter to skip:");
            String input = ConsoleInputReader.readLine(scanner);

            if (input.isEmpty()) {
                return "";
            }

            // Checks for even length
            if (input.length() % 2 != 0) {
                System.out.println("Error: Plugs string must be of even length (pairs).");
                continue;
            }
            return input;
        }
    }
}