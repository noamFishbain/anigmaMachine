package ui;

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

    public String readValidRotorIDs() {
        while (true) {
            System.out.println("Enter Rotor IDs (Left to Right, comma separated): ");
            // We assume user enters Left to Right. The Engine reverses it
            String input = ConsoleInputReader.readLine(scanner).trim();

            if (input.isEmpty()) {
                System.out.println("Error: Input cannot be empty.");
                continue;
            }

            if (!input.matches("^[0-9, ]+$")) {
                System.out.println("Error: Rotor IDs must be numeric. Please try again.");
                continue;
            }

            return input;
        }
    }

    public String readValidPositions(int expectedLength) {
        while (true) {
            System.out.println("Enter Initial Positions (English letters only): ");
            String input = ConsoleInputReader.readLine(scanner).trim().toUpperCase();

            if (input.length() != expectedLength) {
                System.out.println("Error: You entered " + input.length() + " positions, but selected " + expectedLength + " rotors. Please try again.");
                continue;
            }

            if (!input.matches("^[A-Z]+$")) {
                System.out.println("Error: Positions must contain English letters only (A-Z). Please try again.");
                continue;
            }

            return input;
        }
    }

    public int readValidReflectorID() {
        while (true) {
            System.out.println("Enter Reflector ID (1=I, 2=II, 3=III, 4=IV, 5=V): ");
            int input = ConsoleInputReader.readInt(scanner);

            if (input < 1 || input > 5) {
                System.out.println("Error: Reflector ID must be between 1 and 5. Please try again.");
                continue;
            }

            return input;
        }
    }
}