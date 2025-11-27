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
        System.out.println("3. Process text");
        System.out.println("4. Reset machine");
        System.out.println("5. Exit");
        System.out.print("Choose an option (1-5): ");
    }
}
