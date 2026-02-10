package controllers;

import logic.engine.EnigmaEngine;
import logic.engine.MachineSpecs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dto.ManualConfigDTO;
import service.SessionManager;
import dto.EnigmaConfigDTO;

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

    // Handles POST requests to generate and set an automatic machine code
    @PostMapping(value = "/automatic", produces = "application/json")
    public ResponseEntity<Object> setAutomaticCode(@RequestParam("sessionID") String sessionID) {

        // Retrieve the engine instance associated with the session ID
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        // Validate if the session exists
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        try {
            engine.setAutomaticCode();
            MachineSpecs specs = engine.getMachineSpecs();
            String generatedCode = specs.getCurrentCodeCompact();

            return ResponseEntity.ok(Map.of("machineCode", generatedCode));

        } catch (Exception e) {

            // Handle any errors during code generation
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to set automatic code: " + e.getMessage()));
        }
    }

    // Handles POST requests to set the machine configuration manually
    @PostMapping(value = "/manual", produces = "application/json")
    public ResponseEntity<Object> setManualCode(
            @RequestParam("sessionID") String sessionID,
            @RequestBody ManualConfigDTO manualConfig) {

        // Retrieve the engine instance associated with the session ID
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        // Validate if the session exists (Handle "Session Not Found")
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        try {
            // Invoke the engine to set the manual configuration.
            String configuredCode = engine.setManualCode(
                    manualConfig.getRotors(),
                    manualConfig.getPositions(),
                    manualConfig.getReflector(),
                    manualConfig.getPlugs()
            );

            return ResponseEntity.ok(Map.of(
                    "status", "Code configured successfully",
                    "machineCode", configuredCode
            ));

        } catch (Exception e) {
            // Handle any logic/validation errors thrown by the engine
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Handles POST requests to reset the machine to its original configuration
    @PostMapping(value = "/reset", produces = "application/json")
    public ResponseEntity<Object> resetMachine(@RequestParam("sessionID") String sessionID) {

        // Retrieve the engine
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        // Validate session
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        try {
            // Perform the reset in the engine
            engine.reset();

            // Get the current (reset) code to show the user
            MachineSpecs specs = engine.getMachineSpecs();
            String currentCode = specs.getCurrentCodeCompact();

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "status", "Machine reset successfully",
                    "currentCode", currentCode
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}