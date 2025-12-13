package logic.loader.dto;

import java.util.Collections;
import java.util.List;

/**
 * Represents a static definition of a Reflector, as loaded from the configuration file.
 * Contains the ID and the reflection pairs mapping.
 */
public class ReflectorDescriptor {

    private final String id;
    private final List<int[]> pairs;   // each pair represents a mapping (a <-> b)

    public ReflectorDescriptor(String id, List<int[]> pairs) {
        this.id = id;
        // Defensive copy or unmodifiable view is recommended
        this.pairs = pairs != null ? Collections.unmodifiableList(pairs) : Collections.emptyList();
    }

    public String getId() {

        return id;
    }

    public List<int[]> getPairs() {

        return pairs;
    }
}
