package client;

import java.util.Scanner;

import static ui.EscapeSequences.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChessClient {

    private static String serverUrl = "";
    public ChessClient(String givenServerUrl){
        serverUrl = givenServerUrl;
    }
    public static void run() {
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("♕ 240 Chess Client. Type 'help' to get started.");
        LoggedOutLoop(inputScanner);
        //Logged out

    }

    public static void LoggedOutLoop(Scanner inputScanner)
    {
        boolean stayInLoop = true;
        while (stayInLoop){
            System.out.print("[LOGGED OUT] >>> ");
            String command = inputScanner.nextLine();
            command = command.toLowerCase();
            switch (command) {
                case "help":
                    System.out.println("  " + SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>"
                            + SET_TEXT_COLOR_MAGENTA + " - Register a new account.");
                    System.out.println("  " + SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>"
                            + SET_TEXT_COLOR_MAGENTA + " - Log into an existing account.");
                    System.out.println("  " + SET_TEXT_COLOR_BLUE + "help"
                            + SET_TEXT_COLOR_MAGENTA + " - see a list of available commands.");
                    System.out.println("  " + SET_TEXT_COLOR_BLUE + "quit"
                            + SET_TEXT_COLOR_MAGENTA + " - exit the program." + SET_TEXT_COLOR_WHITE);

                    break;
                case "login":
                    System.out.println("Login not implemented yet");

                    break;
                case "register":
                    System.out.println("Register not implemented yet");
                    break;
                case "quit":
                    System.out.println("Quit not implemented yet");
                    stayInLoop = false;
                    break;
                default:
                    System.out.println("Unrecognized command");
                    break;
            }
        }
    }
}