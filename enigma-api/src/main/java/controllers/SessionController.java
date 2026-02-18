package controllers;

import logic.engine.EnigmaEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.EngineManager;
import service.SessionManager;

import java.util.Map;

@RestController
@RequestMapping("/enigma/session") // Base URL for all actions in this controller
public class SessionController {

    private final EngineManager engineManager;
    private final SessionManager sessionManager;

    // Constructor Injection
    public SessionController(EngineManager engineManager, SessionManager sessionManager) {
        this.engineManager = engineManager;
        this.sessionManager = sessionManager;
    }

    // Creates a new session for a specific machine
    @PostMapping
    public ResponseEntity<Object> createSession(@RequestBody Map<String, String> requestBody) {
        String machineName = requestBody.get("machine");

        // Validate that the machine exists in the system
        if (!engineManager.isMachineExists(machineName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Unknown machine name: " + machineName));
        }

        // Create a private copy of the engine for this session
        EnigmaEngine engineCopy = engineManager.createEngineInstance(machineName);

        // Create the session and store the engine
        String sessionId = sessionManager.createSession(engineCopy);

        // Return the new Session ID to the client
        return ResponseEntity.ok(Map.of("sessionID", sessionId));
    }

    // Deletes a session
    @DeleteMapping
    public ResponseEntity<Object> deleteSession(@RequestParam("sessionID") String sessionID) {
        if (!sessionManager.isSessionExists(sessionID)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        sessionManager.removeSession(sessionID);
        return ResponseEntity.noContent().build();
    }
}