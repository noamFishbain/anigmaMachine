package logic.engine;

import logic.loader.dto.MachineHistoryRecord;

import java.io.IOException;
import java.util.List;

/**
 * EnigmaEngine is the "brain" of the application
 * It coordinates between:
 * - The loaded machine structure (from XML)
 * - The current runtime machine instance
 * - Code configuration (manual or automatic)
 * - Statistics and history
 * The UI layer communicates only with this interface
 */
public interface EnigmaEngine {

    // Loads machine structure from XML file
    void loadMachineFromXml(String path) throws Exception;

    // Sets a manual code configuration based on user input
    String setManualCode(String rotorIDsString, String positionsString, int reflectorNum) throws Exception;

    // Generates random configuration
    void setAutomaticCode();

    // Returns MachineSpecs - metadata about the loaded machine
    MachineSpecs getMachineSpecs();

    // Processes a message through the machine
    String process(String text);

    // Resets machine to the original code configuration chosen last time
    void reset();

    // Toggles the verbose debug mode on the internal machine
    void setDebugMode(boolean debugMode);

    // Returns the list of processed messages history and statistics
    List<MachineHistoryRecord> getHistory();
    public int getRequiredRotorCount();

    void loadGame(String pathWithoutExtension) throws IOException, ClassNotFoundException;
    void saveGame(String pathWithoutExtension) throws IOException;
    boolean isCodeConfigurationSet();

}
