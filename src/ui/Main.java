package ui;

/**
 * Entry point of the console-based Enigma application.
 * This class simply creates and starts the ConsoleApp, which
 * handles all user interaction and delegates operations to the engine.
 */
public class Main {

    public static void main(String[] args) {
        ConsoleApp app = new ConsoleApp();
        app.start();
    }
}
