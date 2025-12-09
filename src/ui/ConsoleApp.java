package ui;

import logic.engine.EnigmaEngine;
import logic.engine.EnigmaEngineImpl;
import logic.engine.MachineSpecs;

import java.util.Scanner;

/**
 * Console-based UI layer for interacting with the Enigma engine.
 * This class is responsible for:
 *  - Displaying a menu of available operations (via ConsoleMenu)
 *  - Reading user input from the console (via ConsoleInputReader)
 *  - Delegating actions to the EnigmaEngine implementation
 *  - Printing results and error messages back to the user
 *
 * The console app does NOT contain business logic. All core operations
 * (loading XML, processing text, resetting the machine, etc.) are handled
 * by the engine layer.
 */
public class ConsoleApp {

    private final EnigmaEngine engine;
    private final Scanner scanner;

    public ConsoleApp() {
        this.engine = new EnigmaEngineImpl();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main menu loop. The method will keep running until
     * the user chooses the "Exit" option.
     */
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
                    case 6:
                        handleReset();
                        break;
                    case 7:
                        // handleHistory(); // todo
                        System.out.println("History not implemented yet.");
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

    /**
     * Handles option 1: loading the machine configuration from an XML file.
     * Asks the user for a file path and delegates to the engine.
     */
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

    /**
     * Handles option 2: displaying the current machine specifications.
     * Uses MachineSpecs returned by the engine.
     */
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

    // Command 3: Manual Code
    private void handleManualCode() {
        try {
            // Get Rotors
            System.out.println("Enter Rotor IDs (Left to Right): ");
            // We assume user enters Left to Right. The Engine reverses it.
            String rotorIDs = ConsoleInputReader.readLine(scanner);

            // Get Positions
            System.out.println("Enter Initial Positions: ");
            String positions = ConsoleInputReader.readLine(scanner);

            // Get Reflector
            System.out.println("Enter Reflector ID (1=I, 2=II, 3=III, 4=IV, 5=V): ");
            int reflectorNum = ConsoleInputReader.readInt(scanner);

            // Send to Engine
            String result = engine.setManualCode(rotorIDs, positions, reflectorNum);
            System.out.println("Code set successfully: " + result);

        } catch (Exception e) {
            System.out.println("Failed to set manual code: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    // Command 4: Automatic Code
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

    /**
     * Handles option 3: processing input text through the Enigma machine.
     * At this stage, the engine may simply echo the text back until
     * encryption logic is implemented.
     */
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

    /**
     * Handles option 4: resetting the machine to its original configuration.
     * Currently, if reset() is still empty in the engine, this will effectively
     * be a no-op but the UI flow is already in place.
     */
    private void handleReset() {
        try {
            engine.reset();
            System.out.println("Machine reset to original code.");
        } catch (Exception e) {
            System.out.println("Failed to reset machine: " + e.getMessage());
        }
    }
}