package controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import service.EngineManager;

@RestController
public class HealthController {

    private final EngineManager engineManager;

    public HealthController(EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @GetMapping("/health")
    public String checkHealth() {
        return engineManager.getHealthCheck();
    }
}