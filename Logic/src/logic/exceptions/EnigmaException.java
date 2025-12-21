package logic.exceptions;

/**
 * A centralized exception handler for the Enigma Machine project.
 * This class extends RuntimeException to allow unchecked throwing across the engine.
 * It uses an internal Enum (ErrorCode) to manage all error message templates in one place.
 */
public class EnigmaException extends RuntimeException {

    /**
     * Enum defining all possible error types in the system and their message templates.
     */
    public enum ErrorCode {
        // ------------------------- XML Loading & Validation Errors -------------------------
        FILE_NOT_FOUND("Error: The file '%s' was not found."),
        FILE_NOT_XML_TYPE("Error: The file '%s' must be an XML file."),
        XML_ABC_ODD_LENGTH("Error: ABC size must be even. Current size: %d."),
        XML_ROTOR_COUNT_LOW("Error: Not enough rotors defined. Minimum %d expected."),
        XML_ROTOR_ID_SEQUENCE("Error: Rotor IDs must be unique and sequential (1 to N)."),
        XML_ROTOR_MAPPING_SIZE("Error: Rotor ID %d positioning count does not match ABC size."),
        XML_ROTOR_INVALID_CHARS("Error: Rotor %d contains invalid characters not in ABC: %s, %s."),
        XML_ROTOR_DUPLICATE_RIGHT("Error: Rotor %d maps source char '%s' more than once (Duplicate Mapping)."),
        XML_ROTOR_DUPLICATE_LEFT("Error: Rotor %d maps target char '%s' more than once (Target Duplicate)."),
        XML_REFLECTOR_ID_SEQUENCE("Error: Reflector IDs must be unique and sequential (I to N)."),
        XML_REFLECTOR_UNKNOWN_ID("Error: Unknown reflector ID: %s."),

        // ------------------------- Rotor Component Errors -------------------------
        ROTOR_MAPPING_MISSING("Error: Rotor mapping cannot be null or empty."),
        ROTOR_NOTCH_OUT_OF_RANGE("Error: Notch position %d out of range (valid: 0..%d)."),
        ROTOR_POSITION_OUT_OF_RANGE("Error: Position %d out of range (valid: 0..%d)."),
        ROTOR_BROKEN_MAPPING("Error: Mapping error: Connection not found in Rotor %d."), // Critical logic error

        // ------------------------- Reflector Component Errors -------------------------
        REFLECTOR_OUT_OF_RANGE("Error: Index %d out of range for reflector."),
        REFLECTOR_SIZE_MUST_BE_POSITIVE("Error: Keyboard size must be positive."),
        REFLECTOR_SIZE_ODD("Error: Keyboard size must be even (required for pairing). Got: %d."),
        REFLECTOR_MAPPING_OUT_OF_BOUNDS("Error: Reflector mapping out of range: mapping[%d] = %d."),
        REFLECTOR_SELF_MAPPING("Error: Reflector cannot map index to itself: %d."),
        REFLECTOR_NOT_SYMMETRIC("Error: Reflector mapping is not symmetric: %d <-> %d."),

        // ------------------------- Keyboard & Input Errors -------------------------
        KEYBOARD_EMPTY("Error: Keyboard cannot be null or empty."),
        KEYBOARD_DUPLICATE_SYMBOLS("Error: Keyboard contains duplicate symbols."),
        KEYBOARD_INVALID_CHAR("Error: Character '%s' is not part of this Keyboard."),
        KEYBOARD_OUT_OF_RANGE("Error: Index %d is out of range (0..%d)."),
        INPUT_INVALID_CHARACTER("Error: Input contains invalid character: '%s'.\nThe machine only accepts: %s"),

        // ------------------------- User Configuration Errors -------------------------
        USER_INPUT_EMPTY("Error: Input cannot be empty."),
        USER_INPUT_NOT_NUMBER("Error: Invalid input. Please enter a numeric value."),
        USER_INVALID_REFLECTOR_INPUT("Error: Reflector selection must be between 1 and 5."),
        USER_INVALID_ROTOR_COUNT("Error: Invalid rotor count. Require exactly %d selected rotors, but got: %d."),
        USER_DUPLICATE_ROTOR_IDS("Error: Rotor IDs must be unique. Duplicates found."),
        USER_ROTOR_NOT_FOUND("Error: Rotor ID %d does not exist in the machine. Available IDs: %s."),
        USER_POSITION_COUNT_MISMATCH("Error: Rotor count (%d) must match the number of starting positions (%d)."),
        USER_INVALID_POSITION_CHAR("Error: Character '%s' is not part of the machine's keyboard."),
        CONFIG_ARGS_NULL("Error: Code configuration arguments cannot be null."),
        USER_REFLECTOR_NOT_FOUND("Error: Reflector ID '%s' is not available."),

        // ------------------------- Engine State & Runtime Errors -------------------------
        MACHINE_NOT_LOADED("Error: Machine is not loaded. Please load an XML file first."),
        CONFIG_NOT_SET("Error: Machine configuration has not been set. Please set the code using option 3 or 4 first."),
        NO_CONFIGURATION_TO_RESET("Error: No configuration to reset to. Please set code first.");

        private final String messageTemplate;

        ErrorCode(String messageTemplate) {
            this.messageTemplate = messageTemplate;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }
    }

    private final ErrorCode errorCode;

    /**
     * Constructs a new EnigmaException with the specified error code and dynamic arguments.
     *
     * @param errorCode The specific type of error from the Enum.
     * @param args      Dynamic arguments to be formatted into the message template (e.g., file names, numbers).
     */
    public EnigmaException(ErrorCode errorCode, Object... args) {
        // Formats the message using the template and the provided arguments
        super(String.format(errorCode.getMessageTemplate(), args));
        this.errorCode = errorCode;
    }

    /**
     * Returns the specific error code associated with this exception.
     * Useful if the UI needs to handle specific error types differently.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}