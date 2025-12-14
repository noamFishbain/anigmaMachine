package ui;

import logic.exceptions.EnigmaException;

import java.util.Scanner;

/**
 * Utility class for standardized console input reading.
 * Provides safe methods for reading integers and strings from the user.
 */
public class ConsoleInputReader {

    public static int readInt(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print(EnigmaException.ErrorCode.USER_INPUT_NOT_NUMBER.getMessageTemplate());
            }
        }
    }

    public static String readLine(Scanner scanner) {
        return scanner.nextLine();
    }
}
