package web.controllers;

import logic.engine.EnigmaEngine;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.service.SessionManager;
import web.dto.EnigmaConfigDTO;

import java.util.Map;

@RestController
@RequestMapping("/enigma/config") // Base URL: http://localhost:8080/enigma/config
public class ConfigController {

    private final SessionManager sessionManager;

    public ConfigController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // Handles GET requests to retrieve machine configuration details
    @GetMapping
    public ResponseEntity<Object> getMachineConfig(@RequestParam("sessionID") String sessionID) {

        // Retrieve the specific engine instance for this session
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        // Validate if the session exists
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        // Create the DTO with the current machine statistics
        EnigmaConfigDTO response = new EnigmaConfigDTO(
                engine.getAllRotorsCount(),      // Total available rotors in the inventory
                engine.getAllReflectorsCount(),  // Total available reflectors
                engine.getProcessedMessages()    // Count of messages processed so far (starts at 0)
        );

        // Return the response with HTTP 200 OK
        return ResponseEntity.ok(response);
    }
}