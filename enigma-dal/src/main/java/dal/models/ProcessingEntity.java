package dal.models;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "processing")
public class ProcessingEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private MachineEntity machine;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "code", columnDefinition = "text")
    private String code;

    @Column(name = "input", columnDefinition = "text")
    private String input;

    @Column(name = "output", columnDefinition = "text")
    private String output;

    @Column(name = "time")
    private Long time; // Execution time in nanoseconds

    public ProcessingEntity() {
    }

    public ProcessingEntity(UUID id, MachineEntity machine, String sessionId, String code, String input, String output, Long time) {
        this.id = id;
        this.machine = machine;
        this.sessionId = sessionId;
        this.code = code;
        this.input = input;
        this.output = output;
        this.time = time;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public MachineEntity getMachine() { return machine; }
    public void setMachine(MachineEntity machine) { this.machine = machine; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }
    public Long getTime() { return time; }
    public void setTime(Long time) { this.time = time; }
}