package web.service;

import logic.engine.EnigmaEngine;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SessionManager {

    // Maps a Session ID to a specific Engine Instance
    private final Map<String, EnigmaEngine> sessionMap = new HashMap<>();

    // Creates a new session, stores the engine, and returns a unique ID
    public String createSession(EnigmaEngine engine) {
        // Generate a random unique identifier
        String sessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);

        sessionMap.put(sessionId, engine);
        return sessionId;
    }

    // Retrieves the engine associated with a specific session ID
    public EnigmaEngine getEngine(String sessionId) {
        return sessionMap.get(sessionId);
    }

    // Removes a session from the manager
    public void removeSession(String sessionId) {
        sessionMap.remove(sessionId);
    }

    // Checks if a session ID exists
    public boolean isSessionExists(String sessionId) {
        return sessionMap.containsKey(sessionId);
    }
}