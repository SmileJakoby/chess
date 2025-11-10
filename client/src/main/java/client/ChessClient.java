package client;

import com.google.gson.Gson;
import datamodel.RegisterResponse;
import model.UserData;

import java.util.Scanner;

import static ui.EscapeSequences.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChessClient {

    private static String serverUrl = "";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

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
            String completeCommand = inputScanner.nextLine();
            completeCommand = completeCommand.toLowerCase();
            var commands = completeCommand.split(" ");
            switch (commands[0]) {
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
                    if (commands.length >= 4)
                        try{
                            UserData newUser = new UserData(commands[1], commands[3], commands[2]);
                            var jsonBody = new Gson().toJson(newUser);
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(new URI(serverUrl + "/user"))
                                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                                    .header("Content-Type", "application/json")
                                    .build();
                            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                            if (response.statusCode() == 200) {
                                RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
                                System.out.println(registerResponse.toString());
                            }
                            else{
                                System.out.println("Error: received status code " + response.statusCode());
                            }
                        }
                        catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    else{
                        System.out.println("Must provide a Username, Password, and Email.");
                    }
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