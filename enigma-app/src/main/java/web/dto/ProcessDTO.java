package web.dto;

/**
 * DTO for capturing the input text to be processed by the Enigma machine
 */
public class ProcessDTO {
    private String text; // The text to encrypt or decrypt

    public ProcessDTO() {
    }

    public ProcessDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}