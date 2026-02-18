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
import static utils.ConfigurationUtils.decodeRoman;

@RestController
@RequestMapping("/enigma/config") // Base URL: http://localhost:8080/enigma/config
public class ConfigurationController {

    private final SessionManager sessionManager;

    public ConfigurationController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // GET /enigma/config - Returns machine status
    @GetMapping(produces = "application/json")
    public ResponseEntity<Object> getMachineConfig(
            @RequestParam("sessionID") String sessionID,
            @RequestParam(value = "verbose", defaultValue = "false") boolean verbose) {

        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        EnigmaConfigDTO response = new EnigmaConfigDTO(
                engine.getAllRotorsCount(),
                engine.getAllReflectorsCount(),
                engine.getProcessedMessages()
        );

        if (verbose) {
            MachineSpecs specs = engine.getMachineSpecs();
            response.originalCodeCompact = specs.getOriginalCodeCompact();
            response.currentRotorsPositionCompact = specs.getCurrentCodeCompact();
        }

        return ResponseEntity.ok(response);
    }

    // Handles PUT requests to generate and set an automatic machine code
    @PutMapping(value = "/automatic", produces = "application/json")
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

    // Handles PUT requests to set the machine configuration manually
    @PutMapping(value = "/manual", produces = "application/json")
    public ResponseEntity<Object> setManualCode(@RequestBody ManualConfigDTO manualConfig) {

        String sessionID = manualConfig.getSessionID();

        // Retrieve the engine instance associated with the session ID
        EnigmaEngine engine = sessionManager.getEngine(sessionID);

        // Validate if the session exists (Handle "Session Not Found")
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionID));
        }

        try {
            String rotorsStr = manualConfig.getRotors().stream()
                    .map(r -> String.valueOf(r.rotorNumber))
                    .collect(java.util.stream.Collectors.joining(","));

            String positionsStr = manualConfig.getRotors().stream()
                    .map(r -> r.rotorPosition)
                    .collect(java.util.stream.Collectors.joining(""));

            String plugsStr = manualConfig.getPlugs().stream()
                    .map(p -> p.plug1 + p.plug2)
                    .collect(java.util.stream.Collectors.joining(""));

            int reflectorInt = decodeRoman(manualConfig.getReflector());

            String configuredCode = engine.setManualCode(rotorsStr, positionsStr, reflectorInt, plugsStr);

            return ResponseEntity.ok(Map.of("status", "Code configured successfully", "machineCode", configuredCode));

        } catch (Exception e) {
            // Handle any logic/validation errors thrown by the engine
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Handles PUT requests to reset the machine to its original configuration
    @PutMapping(value = "/reset", produces = "application/json")
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