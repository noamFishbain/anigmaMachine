package dal.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "machines")
public class MachineEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "rotors_count", nullable = false)
    private int rotorsCount;

    @Column(name = "abc", nullable = false)
    private String abc;

    // Default constructor required by JPA
    public MachineEntity() {
    }

    public MachineEntity(UUID id, String name, int rotorsCount, String abc) {
        this.id = id;
        this.name = name;
        this.rotorsCount = rotorsCount;
        this.abc = abc;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getRotorsCount() { return rotorsCount; }
    public void setRotorsCount(int rotorsCount) { this.rotorsCount = rotorsCount; }

    public String getAbc() { return abc; }
    public void setAbc(String abc) { this.abc = abc; }
}