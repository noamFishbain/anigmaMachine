package logic.engine;

import logic.loader.dto.MachineHistoryRecord;

import java.util.List;

/**
 * EnigmaEngine is the "brain" that coordinates between:
 *  - The loaded machine structure (from the XML file)
 *  - The current runtime Enigma machine instance
 *  - Code configuration (manual or automatic)
 *  - History & statistics (later)
 *
 * The UI communicates ONLY with this interface
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
}
