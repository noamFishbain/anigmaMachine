package dto;

public class EnigmaConfigDTO {
    private final int totalRotors;
    private final int totalReflectors;
    private final int totalProcessedMessages;

    public String originalCodeCompact;
    public String currentRotorsPositionCompact;

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
    public String getOriginalCodeCompact() { return originalCodeCompact; }
    public void setOriginalCodeCompact(String originalCodeCompact) { this.originalCodeCompact = originalCodeCompact; }
    public String getCurrentRotorsPositionCompact() { return currentRotorsPositionCompact; }
    public void setCurrentRotorsPositionCompact(String currentRotorsPositionCompact) { this.currentRotorsPositionCompact = currentRotorsPositionCompact; }
}