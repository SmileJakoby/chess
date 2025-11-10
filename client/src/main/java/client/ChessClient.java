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

    private static final int SIGNED_OUT = 0;
    private static final int SIGNED_IN = 1;

    private static String serverUrl = "";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static String myAuthToken = "";

    private static int authState = SIGNED_OUT;

    public ChessClient(String givenServerUrl){
        serverUrl = givenServerUrl;
    }
    public static void run() {
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("♕ 240 Chess Client. Type 'help' to get started.");

        String result = "";
        while (!result.equals("quit")) {
            if (authState == SIGNED_OUT) {
                System.out.print("[SIGNED OUT] >>> ");
            }
            if (authState == SIGNED_IN) {
                System.out.print("[SIGNED IN] >>> ");
            }
            String line = inputScanner.nextLine();
            line = line.toLowerCase();

            try {
                result = EvalCLI(line);
                System.out.print(SET_TEXT_COLOR_YELLOW + result + "\n" + SET_TEXT_COLOR_WHITE);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }


    }

    private static String EvalCLI(String givenString)
    {
        var commands = givenString.split(" ");
        switch (commands[0]) {
            case "help":
                return printHelp();
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
                            myAuthToken = registerResponse.authToken();
                            authState = SIGNED_IN;
                            return registerResponse.toString();
                        }
                        else{
                            System.out.println("Error: received status code " + response.statusCode());
                        }
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                else{
                    return "Must provide a Username, Password, and Email.";
                }
                break;
            case "quit":
                return "quit";
        }
        return "Unrecognized command";
    }
    private static String printHelp(){
        if (authState == SIGNED_OUT){
            return ("  " + SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>"
                    + SET_TEXT_COLOR_MAGENTA + " - Register a new account.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" +
                    SET_TEXT_COLOR_MAGENTA + " - Log into an existing account.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "help"
                    + SET_TEXT_COLOR_MAGENTA + " - see a list of available commands.\n"
                    + "  " + SET_TEXT_COLOR_BLUE + "quit"
                    + SET_TEXT_COLOR_MAGENTA + " - exit the program.\n" + SET_TEXT_COLOR_WHITE);
        }
        else {
            return ("  " + SET_TEXT_COLOR_BLUE + "create <NAME>"
                    + SET_TEXT_COLOR_MAGENTA + " - Create a game.\n");
        }
    }
}