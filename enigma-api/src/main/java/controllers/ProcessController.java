package controllers;

import dal.models.MachineEntity;
import dal.models.ProcessingEntity;
import dal.repositories.MachineRepository;
import dal.repositories.ProcessingRepository;
import logic.engine.EnigmaEngine;
import logic.engine.MachineSpecs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.SessionManager;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/enigma/process")
public class ProcessController {

    private final SessionManager sessionManager;
    private final ProcessingRepository processingRepository;
    private final MachineRepository machineRepository;

    public ProcessController(SessionManager sessionManager,
                             ProcessingRepository processingRepository,
                             MachineRepository machineRepository) {
        this.sessionManager = sessionManager;
        this.processingRepository = processingRepository;
        this.machineRepository = machineRepository;
    }

    /**
     * POST /enigma/process
     * Processes input text using the machine state linked to the session
     */
    @PostMapping(produces = "application/json")
    public ResponseEntity<Object> processText(
            @RequestParam("sessionId") String sessionId,
            @RequestParam("input") String inputText) {

        // Retrieve the engine instance
        EnigmaEngine engine = sessionManager.getEngine(sessionId);

        // Validate session existence
        if (engine == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unknown sessionID: " + sessionId));
        }

        try {
            // Process the text using the engine
            long startTime = System.nanoTime();
            String processedText = engine.process(inputText);
            long duration = System.nanoTime() - startTime;

            MachineSpecs specs = engine.getMachineSpecs();

            String machineName = engine.getMachineName();

            MachineEntity machineEntity = machineRepository.findByName(machineName)
                    .orElseThrow(() -> new RuntimeException("Machine not found in DB"));

            ProcessingEntity entity = new ProcessingEntity(
                    UUID.randomUUID(),
                    machineEntity,
                    sessionId,
                    specs.getCurrentCodeCompact(), // הקוד שבו בוצעה הפעולה
                    inputText,
                    processedText,
                    duration
            );
            processingRepository.save(entity);

            // Return the result as JSON
            return ResponseEntity.ok(Map.of(
                    "output", processedText,
                    "currentRotorsPositionCompact", specs.getCurrentCodeCompact()
            ));

        } catch (Exception e) {
            // Handle errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}