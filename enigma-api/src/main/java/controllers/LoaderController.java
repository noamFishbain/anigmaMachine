package controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.EngineManager;

import java.util.List;
import java.util.Set;

/**
 * Handles external HTTP requests related to loading and viewing machine configurations
 * Base URL: /enigma
 */
@RestController
@RequestMapping("/enigma")
public class LoaderController {

    private final EngineManager engineManager;

    // Constructor Injection
    public LoaderController(EngineManager engineManager) {

        this.engineManager = engineManager;
    }

    // Handles file upload requests
    @PostMapping("/load")
    public String loadMachine(@RequestParam("file") List<MultipartFile> files) {
        StringBuilder result = new StringBuilder();
        for (MultipartFile file : files) {
            try {
                // Pass the file input stream to the service layer
                String loadedMachineName = engineManager.loadEngine(file.getInputStream());
                result.append("File '").append(file.getOriginalFilename())
                        .append("' loaded successfully as machine: ").append(loadedMachineName).append("\n");
            } catch (IllegalArgumentException e) {
                // Handling "Machine already exists" error
                result.append("Error in file '").append(file.getOriginalFilename())
                        .append("': ").append(e.getMessage()).append("\n");
            } catch (Exception e) {
                // General error
                result.append("Error loading file '").append(file.getOriginalFilename())
                        .append("': ").append(e.getMessage()).append("\n");
            }
        }
        return result.toString();
    }

    // Returns a list of all currently loaded machine names
    @GetMapping("/machines")
    public Set<String> getLoadedMachines() {
        return engineManager.getLoadedMachineNames();
    }
}