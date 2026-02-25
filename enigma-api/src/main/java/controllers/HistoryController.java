package controllers;

import dal.models.ProcessingEntity;
import dal.repositories.ProcessingRepository;
import logic.engine.EnigmaEngine;
import logic.loader.dto.MachineHistoryRecord;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.SessionManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enigma/history")
public class HistoryController {

    private final SessionManager sessionManager;
    private final ProcessingRepository processingRepository;

    public HistoryController(SessionManager sessionManager, ProcessingRepository processingRepository) {
        this.sessionManager = sessionManager;
        this.processingRepository = processingRepository;
    }

    /**
     * GET /enigma/history
     * Returns history by sessionID (memory) or machineName (database)
     */
    @GetMapping(produces = "application/json")
    public ResponseEntity<Object> getHistory(
            @RequestParam(name = "sessionID", required = false) String sessionID,
            @RequestParam(name = "machineName", required = false) String machineName) {

        // Validate that exactly one parameter is provided
        if ((sessionID == null && machineName == null) || (sessionID != null && machineName != null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Exactly one of sessionID or machineName must be provided"));
        }

        // Case A: Session History (Active memory)
        if (sessionID != null) {
            // Retrieve the engine
            EnigmaEngine engine = sessionManager.getEngine(sessionID);
            if (engine == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Unknown sessionID: " + sessionID));
            }

            // Fetch history list from the engine instance
            List<MachineHistoryRecord> rawHistory = engine.getHistory() != null ? engine.getHistory() : Collections.emptyList();

            // Group the session history by the applied configuration string
            Map<String, List<MachineHistoryRecord>> groupedSessionHistory = rawHistory.stream()
                    .collect(Collectors.groupingBy(MachineHistoryRecord::getAppliedConfiguration));

            return ResponseEntity.ok(groupedSessionHistory);
        }

        // Case B: Machine History (Database)
        if (machineName != null) {
            // Fetch records from DB and map them to the DTO format
            List<ProcessingEntity> dbRecords = processingRepository.findAllByMachine_Name(machineName);

// Group the database records by the code column and map to DTO
            Map<String, List<MachineHistoryRecord>> groupedMachineHistory = dbRecords.stream()
                    .collect(Collectors.groupingBy(
                            ProcessingEntity::getCode,
                            Collectors.mapping(entity -> new MachineHistoryRecord(
                                    entity.getInput(),
                                    entity.getOutput(),
                                    entity.getTime(),
                                    entity.getCode()
                            ), Collectors.toList())
                    ));

            return ResponseEntity.ok(groupedMachineHistory);
        }

        return ResponseEntity.badRequest().build();
    }
}