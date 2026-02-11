package dal.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "machines_rotors")
public class MachineRotorEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @Column(name = "rotor_id", nullable = false)
    private int rotorId;

    @Column(name = "notch")
    private Integer notch;

    // Storing the arrays as comma-separated strings
    @Column(name = "wiring_right", columnDefinition = "text")
    private String wiringRight;

    @Column(name = "wiring_left", columnDefinition = "text")
    private String wiringLeft;

    public MachineRotorEntity() {
    }

    // Constructor that accepts the mapping logic and converts it to strings
    public MachineRotorEntity(UUID id, MachineEntity machine, int rotorId, Integer notch, int[][] mapping) {
        this.id = id;
        this.machine = machine;
        this.rotorId = rotorId;
        this.notch = notch;
        setWiringFromMapping(mapping);
    }

    // Converts int[][] mapping to CSV strings for DB storage
    public void setWiringFromMapping(int[][] mapping) {
        StringBuilder right = new StringBuilder();
        StringBuilder left = new StringBuilder();

        for (int i = 0; i < mapping.length; i++) {
            if (i > 0) {
                right.append(",");
                left.append(",");
            }
            right.append(mapping[i][0]);
            left.append(mapping[i][1]);
        }
        this.wiringRight = right.toString();
        this.wiringLeft = left.toString();
    }

    // Converts CSV strings back to int[][] mapping for application usage
    public int[][] getMappingFromWiring(int alphabetSize) {
        int[][] mapping = new int[alphabetSize][2];
        String[] rights = this.wiringRight.split(",");
        String[] lefts = this.wiringLeft.split(",");

        for (int i = 0; i < alphabetSize; i++) {
            mapping[i][0] = Integer.parseInt(rights[i]);
            mapping[i][1] = Integer.parseInt(lefts[i]);
        }
        return mapping;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public MachineEntity getMachine() { return machine; }
    public void setMachine(MachineEntity machine) { this.machine = machine; }
    public int getRotorId() { return rotorId; }
    public void setRotorId(int rotorId) { this.rotorId = rotorId; }
    public Integer getNotch() { return notch; }
    public void setNotch(Integer notch) { this.notch = notch; }
    public String getWiringRight() { return wiringRight; }
    public void setWiringRight(String wiringRight) { this.wiringRight = wiringRight; }
    public String getWiringLeft() { return wiringLeft; }
    public void setWiringLeft(String wiringLeft) { this.wiringLeft = wiringLeft; }
}