package controllers;

import logic.engine.EnigmaEngine;
import logic.loader.dto.MachineHistoryRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dto.ProcessDTO;
import service.SessionManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/enigma") // Base URL for operations: http://localhost:8080/enigma
public class EncryptionController {

    private final SessionManager sessionManager;

    public EncryptionController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // Handles POST requests to process (encrypt/decrypt) a message
    @PostMapping(value = "/process", produces = "application/json")
    public ResponseEntity<Object> processText(
            @RequestParam("sessionID") String sessionID,
            @RequestBody ProcessDTO request) {

        // Retrieve the engine instance
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        // Validate session existence
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        try {
            // Process the text using the engine
            String processedText = engine.process(request.getText());

            // Return the result as JSON
            return ResponseEntity.ok(Map.of(
                    "status", "Message processed successfully",
                    "processedText", processedText
            ));

        } catch (Exception e) {
            // Handle errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Handles GET requests to retrieve the machine's processing history
    @GetMapping(value = "/history", produces = "application/json")
    public ResponseEntity<Object> getHistory(@RequestParam("sessionID") String sessionID) {

        // Retrieve the engine
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        // Get history list from the engine
        List<MachineHistoryRecord> history = engine.getHistory();

        // Return the list (or empty list if null)
        return ResponseEntity.ok(history != null ? history : Collections.emptyList());
    }
}