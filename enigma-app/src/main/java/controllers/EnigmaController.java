package controllers;

import logic.loader.XmlMachineConfigLoader;
import logic.loader.dto.MachineDescriptor;
import service.DBStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/enigma")
public class EnigmaController {

    private final DBStorageService dbStorageService;
    private final XmlMachineConfigLoader loader;

    @Autowired
    public EnigmaController(DBStorageService dbStorageService) {
        this.dbStorageService = dbStorageService;
        this.loader = new XmlMachineConfigLoader(); // Using your loader which has its own validator and converter
    }

    /**
     * Requirement 4: Upload XML file, validate it, and save to DB.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadMachine(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Load and Validate using your existing logic
            MachineDescriptor descriptor = loader.loadDescriptor(file.getInputStream());

            // 2. Set machine name from filename (Requirement 4 mentions machines appear by name)
            String machineName = file.getOriginalFilename() != null ?
                    file.getOriginalFilename().replace(".xml", "") :
                    "Unknown_Machine_" + System.currentTimeMillis();
            descriptor.setName(machineName);

            // 3. Save to Database using the service we built
            dbStorageService.saveMachine(descriptor);

            return ResponseEntity.ok("Machine '" + machineName + "' uploaded and saved to DB successfully.");

        } catch (Exception e) {
            // Here you can use your EnigmaException logic to return specific error messages
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }
}