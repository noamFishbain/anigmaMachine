package logic.loader;

import logic.loader.dto.MachineDescriptor;
import logic.machine.Machine;

public interface MachineConfigLoader {

    /**
     * Loads an Enigma machine configuration from the given file path
     * and returns a high–level descriptor of the machine.
     *
     * @param filePath path to the XML configuration file
     * @return MachineDescriptor representing the machine configuration
     * @throws Exception if the file is missing, invalid, or fails validation
     */
    Machine load(String filePath) throws Exception;
}