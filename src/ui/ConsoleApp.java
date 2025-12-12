package ui;

import logic.engine.EnigmaEngine;
import logic.engine.EnigmaEngineImpl;
import logic.engine.MachineSpecs;
import logic.loader.dto.MachineHistoryRecord;

import java.util.List;
import java.util.Scanner;

/** Console-based UI layer for interacting with the Enigma engine */
public class ConsoleApp {

    private final EnigmaEngine engine;
    private final Scanner scanner;

    public ConsoleApp() {
        this.engine = new EnigmaEngineImpl();
        this.scanner = new Scanner(System.in);
    }

    // Starts the main menu loop
    public void start() {
        boolean exit = false;

        while (!exit) {
            ConsoleMenu.printMainMenu();

            // Using ConsoleInputReader to safely read an integer option
            int choice = ConsoleInputReader.readInt(scanner);

            try {
                switch (choice) {
                    case 1:
                        handleLoadXml();
                        break;
                    case 2:
                        handleShowMachineSpecs();
                        break;
                    case 3:
                        handleManualCode();
                        break;
                    case 4:
                        handleAutomaticCode();
                        break;
                    case 5:
                        handleProcessText();
                        break;
                    case 6:
                        handleReset();
                        break;
                    case 7:
                        handleHistory();
                        break;
                    case 8:
                        exit = true;
                        System.out.println("Exiting application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1-8.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println(); // blank line between iterations
        }

        scanner.close();
    }

    // Command 1: Loads the machine configuration from an XML file
    private void handleLoadXml() {
        System.out.print("Enter full path to XML file: ");
        String path = ConsoleInputReader.readLine(scanner).trim();

        try {
            engine.loadMachineFromXml(path);
            engine.setDebugMode(true);
            System.out.println("Machine configuration loaded successfully.");
        } catch (Exception e) {
            System.out.println("Failed to load machine from XML:");
            System.out.println(e.getMessage());
        }
    }

    // Command 2: Displays the current machine specifications
    private void handleShowMachineSpecs() {
        try {
            MachineSpecs specs = engine.getMachineSpecs();

            System.out.println("----- Machine Specifications -----");
            System.out.println("Total rotors defined:      " + specs.getTotalRotors());
            System.out.println("Total reflectors defined:  " + specs.getTotalReflectors());
            System.out.println("Processed messages:        " + specs.getTotalProcessedMessages());

            String originalCode = specs.getOriginalCodeCompact();
            String currentCode = specs.getCurrentCodeCompact();

            System.out.println("Original code: " +
                    (originalCode != null ? originalCode : "<not set>"));
            System.out.println("Current code:  " +
                    (currentCode != null ? currentCode : "<not set>"));

        } catch (IllegalStateException e) {
            System.out.println("Machine is not loaded yet. Please load an XML file first.");
        } catch (Exception e) {
            System.out.println("Failed to retrieve machine specifications: " + e.getMessage());
        }
    }

    // Command 3: Sets up the Manual Code configuration
    private void handleManualCode() {
        try {

            // Get Rotors
            String rotorIDs = readValidRotorIDs();

            // Calculate how many rotors were selected to check for length
            int rotorsCount = rotorIDs.split("[, ]+").length;

            if (rotorsCount != 3) {
                System.out.println("Error: You must select exactly 3 rotors. You selected " + rotorsCount + ".");
                System.out.println("Please try again.");
                return;
            }

            // Get Positions
            String positions = readValidPositions(rotorsCount);

            // Get Reflector
            int reflectorNum = readValidReflectorID();

            // Send to Engine
            String result = engine.setManualCode(rotorIDs, positions, reflectorNum);
            System.out.println("Code set successfully: " + result);

        } catch (Exception e) {
            System.out.println("Failed to set manual code: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    private String readValidRotorIDs() {
        while (true) {
            System.out.println("Enter Rotor IDs (Left to Right, comma separated): ");
            // We assume user enters Left to Right. The Engine reverses it.
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

    private String readValidPositions(int expectedLength) {
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

    private int readValidReflectorID() {
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

    // Command 4: Sets up the Automatic Code configuration
    private void handleAutomaticCode() {
        try {
            engine.setAutomaticCode();
            MachineSpecs specs = engine.getMachineSpecs();
            System.out.println("Automatic code generated successfully.");
            System.out.println("Selected Code: " + specs.getOriginalCodeCompact());
        } catch (Exception e) {
            System.out.println("Failed to set automatic code: " + e.getMessage());
        }
    }

    // Command 5: Processes input text through the machine
    private void handleProcessText() {
        System.out.print("Enter text to process: ");
        String input = ConsoleInputReader.readLine(scanner);

        try {
            String output = engine.process(input);
            System.out.println("Input : " + input);
            System.out.println("Output: " + output);
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Failed to process text: " + e.getMessage());
        }
    }

    // Command 6: Resets the machine to its original configuration
    private void handleReset() {
        try {
            engine.reset();
            System.out.println("Machine reset to original code.");
        } catch (Exception e) {
            System.out.println("Failed to reset machine: " + e.getMessage());
        }
    }

    // Command 7: Displays history and statistics
    private void handleHistory() {
        List<MachineHistoryRecord> history = engine.getHistory();

        if (history.isEmpty()) {
            System.out.println("No history to display yet.");
            return;
        }

        System.out.println("----- Machine History -----");
        int counter = 1;
        for (MachineHistoryRecord entry : history) {
            System.out.println("#" + counter++);
            System.out.println(entry);
            System.out.println("---------------------------");
        }
    }
}