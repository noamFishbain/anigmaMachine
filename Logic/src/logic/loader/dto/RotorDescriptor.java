package logic.loader.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents a static definition of a Rotor, as loaded from the configuration file.
 * This object is immutable
 */
public class RotorDescriptor {

    private final int id;
    private final List<Integer> mapping;   // Mapping from input index to output index
    private final int notchPosition;

    public RotorDescriptor(int id, List<Integer> mapping, int notchPosition) {
        this.id = id;
        // Defensive copy to ensure list cannot be modified from outside
        this.mapping = mapping != null ? Collections.unmodifiableList(mapping) : Collections.emptyList();
        this.notchPosition = notchPosition;
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
}
