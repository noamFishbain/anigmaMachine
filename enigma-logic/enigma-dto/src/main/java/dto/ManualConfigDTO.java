package dto;

import java.util.List;

public class ManualConfigDTO {

    private List<RotorConfig> rotors; // A comma-separated string of rotor IDs
    private String reflector;  // The ID of the selected reflector
    private String positions; // A string representing the starting characters for each rotor
    private List<PlugConfig> plugs; // A string representing the plugboard connections
    private String sessionID;

    public static class RotorConfig {
        public int rotorNumber;
        public String rotorPosition;
    }

    public static class PlugConfig {
        public String plug1;
        public String plug2;
    }

    public ManualConfigDTO() {
    }

    public ManualConfigDTO(List<RotorConfig> rotors, String reflector, String positions, List<PlugConfig> plugs) {
        this.rotors = rotors;
        this.reflector = reflector;
        this.positions = positions;
        this.plugs = plugs;
    }

    public List<RotorConfig> getRotors() {
        return rotors;
    }

    public void setRotors(List<RotorConfig> rotors) {
        this.rotors = rotors;
    }

    public String getReflector() {
        return reflector;
    }

    public void setReflector(String reflector) {
        this.reflector = reflector;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public List<PlugConfig> getPlugs() {
        return plugs;
    }

    public void setPlugs(List<PlugConfig> plugs) {
        this.plugs = plugs;
    }

    public String getSessionID() { return sessionID; }

    public void setSessionID(String sessionID) { this.sessionID = sessionID; }
}