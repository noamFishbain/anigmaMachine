package logic.engine;

public interface EnigmaEngine {
    void loadMachineFromXml(String path) throws Exception;
    MachineSpecs getMachineSpecs();
    // void setManualCode(...);
    String process(String text);
    void reset();
}
