package dto;

public class ManualConfigDTO {

    private String rotors; // A comma-separated string of rotor IDs
    private int reflector;  // The ID of the selected reflector
    private String positions; // A string representing the starting characters for each rotor
    private String plugs; // A string representing the plugboard connections

    public ManualConfigDTO() {
    }

    public ManualConfigDTO(String rotors, int reflector, String positions, String plugs) {
        this.rotors = rotors;
        this.reflector = reflector;
        this.positions = positions;
        this.plugs = plugs;
    }

    public String getRotors() {
        return rotors;
    }

    public void setRotors(String rotors) {
        this.rotors = rotors;
    }

    public int getReflector() {
        return reflector;
    }

    public void setReflector(int reflector) {
        this.reflector = reflector;
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = positions;
    }

    public String getPlugs() {
        return plugs;
    }

    public void setPlugs(String plugs) {
        this.plugs = plugs;
    }
}