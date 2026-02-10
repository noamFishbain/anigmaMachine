package logic.loader;

import logic.machine.Machine;

public interface MachineConfigLoader {

    /**
     * Loads an Enigma machine configuration from the given file path
     * and returns a high-level descriptor of the machine
     */
    Machine load(String filePath) throws Exception;
}