package web.dto;

public class EnigmaConfigDTO {
    private final int totalRotors;
    private final int totalReflectors;
    private final int totalProcessedMessages;

    public EnigmaConfigDTO(int totalRotors, int totalReflectors, int totalProcessedMessages) {
        this.totalRotors = totalRotors;
        this.totalReflectors = totalReflectors;
        this.totalProcessedMessages = totalProcessedMessages;
    }

    public int getTotalRotors() {
        return totalRotors;
    }

    public int getTotalReflectors() {
        return totalReflectors;
    }

    public int getTotalProcessedMessages() {
        return totalProcessedMessages;
    }
}