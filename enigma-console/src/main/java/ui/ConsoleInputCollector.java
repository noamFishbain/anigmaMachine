package ui;

import logic.exceptions.EnigmaException;

import java.util.*;

/**
 * Responsible for collecting and validating raw user input
 * for the Enigma code configuration (Rotors, Positions, Reflector ID).
 */
public class ConsoleInputCollector {

    private final Scanner scanner;

    public ConsoleInputCollector(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readValidRotorIDs(int requiredCount, int maxRotorId) {
        while (true) {
            System.out.printf("Enter %d Rotor IDs (Left to Right, comma separated): %n", requiredCount);
            // We assume user enters Left to Right. The Engine reverses it
            String input = ConsoleInputReader.readLine(scanner).trim();

            // Check for empty input
            if (input.isEmpty()) {
                System.out.println(EnigmaException.ErrorCode.USER_INPUT_EMPTY.getMessageTemplate());
                continue;
            }

            // Check syntax (only numbers, commas, and spaces allowed)
            if (!validateSyntax(input)) {
                continue;
            }

            // Check for maching rotors count input
            if (!validateCount(input, requiredCount)) {
                continue;
            }

            // Logical validation (Range and Duplicates)
            if (!isRotorInputValid(input, maxRotorId)) {
                // Error messages are printed inside the helper method
                continue;
            }

            return input;
        }
    }

    // Helper method to validate specific rotor logic
    private boolean isRotorInputValid(String input, int maxRotorId) {
        String[] parts = input.split("[, ]+");
        List<Integer> invalidIds = new ArrayList<>();
        List<Integer> duplicateCheck = new ArrayList<>();
        boolean hasError = false;

        for (String part : parts) {
            try {
                int id = Integer.parseInt(part);

                // Range Check: Ensure ID exists in the machine
                if (id < 1 || id > maxRotorId) {
                    invalidIds.add(id);
                }

                // Duplicate Check: Ensure the user didn't select the same rotor twice
                if (duplicateCheck.contains(id)) {
                    System.out.println("Error: Duplicate Rotor ID entered: " + id);
                    hasError = true;
                }
                duplicateCheck.add(id);

            } catch (NumberFormatException ignored) {
            }
        }

        // Report non-existent rotors
        if (!invalidIds.isEmpty()) {
            System.out.println("Error: The following Rotor IDs do not exist: " + invalidIds);
            System.out.println("Available IDs range: 1 - " + maxRotorId);
            return false;
        }

        return !hasError;
    }

    // Check syntax (only numbers, commas, and spaces allowed)
    private boolean validateSyntax(String input) {
        if (!input.matches("^[0-9, ]+$")) {
            System.out.println(EnigmaException.ErrorCode.USER_INPUT_NOT_NUMBER.getMessageTemplate());
            return false;
        }
        return true;
    }

    private boolean validateCount(String input, int requiredCount) {
        int count = input.split("[, ]+").length;
        if (count != requiredCount) {
            System.out.println("Error: You must select exactly " + requiredCount + " rotors. You selected " + count + ".");
            return false;
        }
        return true;
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

    public int readValidReflectorID(List<Integer> availableIDs) {
        while (true) {
            System.out.println("Enter Reflector ID (1=I, 2=II, 3=III, 4=IV, 5=V): ");
            int input = ConsoleInputReader.readInt(scanner);
            try {
                if (input < 1 || input > 5) {
                    throw new EnigmaException(EnigmaException.ErrorCode.USER_INVALID_REFLECTOR_INPUT);
                }
                if (!availableIDs.contains(input)) {
                    throw new EnigmaException(
                            EnigmaException.ErrorCode.USER_REFLECTOR_ID_NOT_IN_MACHINE,
                            input,
                            availableIDs.toString()
                    );
                }
                return input;
            }
            catch (EnigmaException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Reads plugboard settings from the user
    public String readValidPlugs(String machineABC) {
        while (true) {
            System.out.println("Enter plug pairs without separation (e.g. ABZD) or press Enter to skip:");
            String input = ConsoleInputReader.readLine(scanner).trim().toUpperCase();

            if (input.isEmpty()) {
                return "";
            }

            try {
                // 1. Check Even Length
                if (input.length() % 2 != 0) {
                    throw new EnigmaException(EnigmaException.ErrorCode.USER_PLUG_ODD_LENGTH);
                }

                // Used to track characters we've already seen in the plugs
                Set<Character> usedChars = new HashSet<>();

                // Iterate over pairs
                for (int i = 0; i < input.length(); i += 2) {
                    char c1 = input.charAt(i);
                    char c2 = input.charAt(i + 1);

                    // 2. Check Valid Characters against Machine ABC
                    if (machineABC.indexOf(c1) == -1) {
                        throw new EnigmaException(EnigmaException.ErrorCode.USER_PLUG_INVALID_CHAR, c1);
                    }
                    if (machineABC.indexOf(c2) == -1) {
                        throw new EnigmaException(EnigmaException.ErrorCode.USER_PLUG_INVALID_CHAR, c2);
                    }

                    // 3. Check Self Mapping (e.g. 'AA')
                    if (c1 == c2) {
                        throw new EnigmaException(EnigmaException.ErrorCode.USER_PLUG_SELF_MAPPING, c1);
                    }

                    // 4. Check Duplicates (e.g. 'ABAC' -> A is used twice)
                    if (!usedChars.add(c1)) {
                        throw new EnigmaException(EnigmaException.ErrorCode.USER_PLUG_ALREADY_USED, c1);
                    }
                    if (!usedChars.add(c2)) {
                        throw new EnigmaException(EnigmaException.ErrorCode.USER_PLUG_ALREADY_USED, c2);
                    }
                }

                // If we got here, input is completely valid
                return input;

            } catch (EnigmaException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}