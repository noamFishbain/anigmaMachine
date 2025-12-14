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
    private final ConsoleInputCollector inputCollector;

    public ConsoleApp() {
        this.engine = new EnigmaEngineImpl();
        this.scanner = new Scanner(System.in);
        this.inputCollector = new ConsoleInputCollector(scanner);
    }

    // Starts the main menu loop
    public void start() {
        boolean exit = false;
        ConsoleMenu.printWelcomeMessage();
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
                    // --- NEW CASES ---
                    case 8:
                        handleSaveGame();
                        break;
                    case 9:
                        handleLoadGame();
                        break;
                    // -----------------
                    case 10:
                        exit = true;
                        System.out.println("Exiting application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1-10.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println(); // blank line between iterations
        }

        scanner.close();
    }

    // --- Existing Methods (1-7) ---

    private void handleLoadXml() {
        System.out.print("Enter full path to XML file: ");
        String path = ConsoleInputReader.readLine(scanner).trim();

        try {
            engine.loadMachineFromXml(path);
            engine.setDebugMode(false);
            System.out.println("Machine configuration loaded successfully.");
        } catch (Exception e) {
            System.out.println("Failed to load machine from XML:");
            System.out.println(e.getMessage());
        }
    }

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

    private void handleManualCode() {
        try {
            String rotorIDs = inputCollector.readValidRotorIDs();
            int rotorsCount = rotorIDs.split("[, ]+").length;

            if (rotorsCount != 3) { // Assume 3 is standard for this exercise level
                System.out.println("Error: You must select exactly 3 rotors. You selected " + rotorsCount + ".");
                return;
            }

            String positions = inputCollector.readValidPositions(rotorsCount);
            int reflectorNum = inputCollector.readValidReflectorID();

            String result = engine.setManualCode(rotorIDs, positions, reflectorNum);
            System.out.println("Code set successfully: " + result);

        } catch (Exception e) {
            System.out.println("Failed to set manual code: " + e.getMessage());
        }
    }

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

    private void handleProcessText() {
        // Validation: Check if machine is ready
        if (!isMachineReadyForOperation()) {
            return;
        }

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

    private void handleReset() {
        try {
            engine.reset();
            System.out.println("Machine reset to original code.");
        } catch (Exception e) {
            System.out.println("Failed to reset machine: " + e.getMessage());
        }
    }

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

    // --- New Bonus Methods (8-9) ---

    // Command 8: Saves the current machine state to a file
    private void handleSaveGame() {
        System.out.print("Enter path to save file (without extension): ");
        String path = ConsoleInputReader.readLine(scanner).trim();

        try {
            // Note: If no machine is currently loaded, the Engine implementation might throw an exception,
            // or save a null machine. Ideally, check if machine exists before saving.
            engine.saveGame(path);
            System.out.println("Game saved successfully to " + path + ".dat");
        } catch (Exception e) {
            System.out.println("Failed to save game:");
            System.out.println(e.getMessage());
        }
    }

    // Command 9: Loads a previously saved machine state
    private void handleLoadGame() {
        System.out.print("Enter path to load file (without extension): ");
        String path = ConsoleInputReader.readLine(scanner).trim();

        try {
            engine.loadGame(path);
            // Re-enable debug mode after load if desired, or keep saved state
            engine.setDebugMode(true);
            System.out.println("Game loaded successfully from " + path + ".dat");
        } catch (java.io.FileNotFoundException e) {
            // Specific handling for when the file does not exist
            System.out.println("Error: The file '" + path + ".dat' was not found.");
            System.out.println("Please make sure you have saved a machine state before trying to load.");
        } catch (Exception e) {
            // General error handling (corrupted file, class version mismatch, etc.)
            System.out.println("Failed to load game:");
            System.out.println(e.getMessage());
        }
    }
    // Helper method to validate machine state before processing
    private boolean isMachineReadyForOperation() {
        // 1. Check if the machine is loaded (XML file loaded)
        try {
            MachineSpecs specs = engine.getMachineSpecs();
            if (specs.getTotalRotors() == 0) {
                System.out.println("Error: No machine loaded. Please load XML first (Option 1).");
                return false;
            }
        } catch (Exception e) {
            // Handle case where engine throws exception because machine object is null
            System.out.println("Error: No machine loaded. Please load XML first (Option 1).");
            return false;
        }

        // 2. Check if the code configuration is set (Rotors, Positions, Reflector selected)
        if (!engine.isCodeConfigurationSet()) {
            System.out.println("Error: Machine configuration has not been set.");
            System.out.println("Please set the code manually (Option 3) or automatically (Option 4).");
            return false;
        }

        return true;
    }
}