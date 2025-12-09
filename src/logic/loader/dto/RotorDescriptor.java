package logic.loader.dto;

import java.util.List;

/**
 * Represents a single rotor definition as loaded from the XML configuration.
 * This DTO contains only configuration data and does not implement any logic.
 *
 * Typical fields:
 *  - Rotor ID (unique integer)
 *  - The mapping between input and output positions
 *  - The notch position that triggers the next rotor advance
 */
public class RotorDescriptor {

    private int id;
    private List<Integer> mapping;   // placeholder, can be refined later
    private int notchPosition;

    public RotorDescriptor(int id,
                           List<Integer> mapping,
                           int notchPosition) {
        this.id = id;
        this.mapping = mapping;
        this.notchPosition = notchPosition;
    }

    public RotorDescriptor() {

    }

    public int getId() {
        return id;
    }

    public List<Integer> getMapping() {
        return mapping;
    }

    public int getNotchPosition() {
        return notchPosition;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setMapping(List<Integer> mapping) {
        this.mapping = mapping;
    }
    public void setNotchPosition(int notchPosition) {
        this.notchPosition = notchPosition;
    }


}
