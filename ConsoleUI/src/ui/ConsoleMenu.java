package ui;

/**
 * Responsible only for printing the main console menu to the user.
 * This class contains no logic. It simply encapsulates the menu UI structure.
 */
public class ConsoleMenu {

    public static void printMainMenu() {
        System.out.println("========== Enigma Console ==========");
        System.out.println("1. Load machine from XML file");
        System.out.println("2. Show machine specifications");
        System.out.println("3. Set Manual Code (Rotors, Positions, Reflector)");
        System.out.println("4. Set Automatic Code (Random)");
        System.out.println("5. Process text");
        System.out.println("6. Reset machine");
        System.out.println("7. History and Statistics");
        System.out.println("8. Save Machine State to File");
        System.out.println("9. Load Machine State from File");
        // -------------------------
        System.out.println("10. Exit");
        System.out.print("Choose an option (1-10): ");
    }

    public static void printWelcomeMessage() {
        System.out.println("\n");
        System.out.println("      __________________________________________________________________________ ");
        System.out.println("     |                                                                          |");
        System.out.println("     |                        Created By: Tal & Noam                            |");
        System.out.println("     |                                                                          |");
        System.out.println("     |       ______   _   _   _____    _____   __  __              _            |");
        System.out.println("     |      |  ____| | \\ | | |_   _|  / ____| |  \\/  |     /\\     | |           |");
        System.out.println("     |      | |__    |  \\| |   | |   | |  __  | \\  / |    /  \\    | |           |");
        System.out.println("     |      |  __|   | . ` |   | |   | | |_ | | |\\/| |   / /\\ \\   | |           |");
        System.out.println("     |      | |____  | |\\  |  _| |_  | |__| | | |  | |  / ____ \\  |_|           |");
        System.out.println("     |      |______| |_| \\_| |_____|  \\_____| |_|  |_| /_/    \\_\\ (_)           |");
        System.out.println("     |                                                                          |");
        System.out.println("     |__________________________________________________________________________|");
        System.out.println("\n");

        // השהייה קטנה לאפקט
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }
}