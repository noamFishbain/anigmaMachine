package dal.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "machines_reflectors")
public class MachineReflectorEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @Column(name = "reflector_id", nullable = false)
    private String reflectorId;

    // Storing mapping pairs as comma-separated strings
    @Column(name = "input", columnDefinition = "text")
    private String input;

    @Column(name = "output", columnDefinition = "text")
    private String output;

    public MachineReflectorEntity() {
    }

    public MachineReflectorEntity(UUID id, MachineEntity machine, String reflectorId, int[] mapping) {
        this.id = id;
        this.machine = machine;
        this.reflectorId = reflectorId;
        setMappingFromIntArray(mapping);
    }

    // Converts int[] mapping to CSV strings
    public void setMappingFromIntArray(int[] mapping) {
        StringBuilder inputs = new StringBuilder();
        StringBuilder outputs = new StringBuilder();

        // Since the mapping is symmetric, we store the full mapping here
        for (int i = 0; i < mapping.length; i++) {
            if (i > 0) {
                inputs.append(",");
                outputs.append(",");
            }
            inputs.append(i);
            outputs.append(mapping[i]);
        }
        this.input = inputs.toString();
        this.output = outputs.toString();
    }

    // Converts CSV strings back to int[] mapping
    public int[] getMappingToIntArray(int size) {
        int[] mapping = new int[size];
        String[] ins = this.input.split(",");
        String[] outs = this.output.split(",");

        for (int i = 0; i < ins.length; i++) {
            int inIdx = Integer.parseInt(ins[i]);
            int outIdx = Integer.parseInt(outs[i]);
            mapping[inIdx] = outIdx;
        }
        return mapping;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public MachineEntity getMachine() { return machine; }
    public void setMachine(MachineEntity machine) { this.machine = machine; }
    public String getReflectorId() { return reflectorId; }
    public void setReflectorId(String reflectorId) { this.reflectorId = reflectorId; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
}