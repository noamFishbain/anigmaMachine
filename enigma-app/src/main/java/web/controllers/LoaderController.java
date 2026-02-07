package web.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import web.service.EngineManager;

import java.io.IOException;

@RestController
public class LoaderController {

    private final EngineManager engineManager;

    // Constructor Injection
    public LoaderController(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    // Handles file upload requests
    @PostMapping("/load")
    public String loadMachine(@RequestParam("file") MultipartFile file) {
        try {
            // Pass the file input stream to the service layer
            engineManager.loadEngine(file.getInputStream());
            return "File uploaded successfully";
        } catch (Exception e) {
            // Return error message if loading fails
            return "Error loading file: " + e.getMessage();
        }
    }
}