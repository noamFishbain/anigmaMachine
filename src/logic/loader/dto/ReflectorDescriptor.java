package logic.loader.dto;

import java.util.List;

/**
 * Represents a single reflector definition as loaded from the XML configuration.
 * This DTO contains only the configuration data required to construct a runtime
 * Reflector component in the machine layer.
 *
 * Typical fields:
 *  - Reflector ID (e.g., "I", "II", "III")
 *  - The mapping of index pairs defining how signals are reflected
 */
public class ReflectorDescriptor {

    private final String id;
    private final List<int[]> pairs;   // each pair represents a mapping (a <-> b)

    public ReflectorDescriptor(String id, List<int[]> pairs) {
        this.id = id;
        this.pairs = pairs;
    }

    public String getId() {
        return id;
    }

    public List<int[]> getPairs() {
        return pairs;
    }
}
