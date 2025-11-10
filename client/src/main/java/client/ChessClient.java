package client;

import com.google.gson.Gson;
import datamodel.*;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static ui.EscapeSequences.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ChessClient {

    private static final int LOGGED_OUT = 0;
    private static final int LOGGED_IN = 1;

    private static String serverUrl = "";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static String myAuthToken = "";

    private static int authState = LOGGED_OUT;

    private static final Map<Integer,Integer> IDMap = new HashMap<>();

    public ChessClient(String givenServerUrl){
        serverUrl = givenServerUrl;
    }
    public static void run() {
        Scanner inputScanner = new Scanner(System.in);
        System.out.println("♕ 240 Chess Client. Type 'help' to get started.");

        String result = "";
        while (!result.equals("Goodbye!")) {
            if (authState == LOGGED_OUT) {
                System.out.print("[LOGGED OUT] >>> ");
            }
            if (authState == LOGGED_IN) {
                System.out.print("[LOGGED IN] >>> ");
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
        if (authState == LOGGED_OUT) {
            switch (commands[0]) {
                case "help":
                    return PrintHelp();
                case "register":
                    return Register(commands);
                case "login":
                    return Login(commands);
                case "quit":
                    return "Goodbye!";
            }
        }
        if (authState == LOGGED_IN) {
            switch (commands[0]) {
                case "help":
                    return PrintHelp();
                case "logout":
                    return Logout();
                case "create":
                    return CreateGame(commands);
                case "list":
                    return ListGames();
                case "join":
                    return JoinGame(commands);
                case "observe":
                    return "observe game not implemented yet";
            }
        }
        return "Unrecognized command";
    }
    private static String PrintHelp(){
        if (authState == LOGGED_OUT){
            return ("  " + SET_TEXT_COLOR_BLUE + "register <USERNAME> <PASSWORD> <EMAIL>"
                    + SET_TEXT_COLOR_MAGENTA + " - Register a new account.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "login <USERNAME> <PASSWORD>" +
                    SET_TEXT_COLOR_MAGENTA + " - Log into an existing account.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "help"
                    + SET_TEXT_COLOR_MAGENTA + " - see a list of available commands.\n"
                    + "  " + SET_TEXT_COLOR_BLUE + "quit"
                    + SET_TEXT_COLOR_MAGENTA + " - exit the program." + SET_TEXT_COLOR_WHITE);
        }
        else {
            return ("  " + SET_TEXT_COLOR_BLUE + "help"
                    + SET_TEXT_COLOR_MAGENTA + " - see a list of available commands.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "logout"
                    + SET_TEXT_COLOR_MAGENTA + " - Logout of your account.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "create <NAME>"
                    + SET_TEXT_COLOR_MAGENTA + " - Create a game.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "list"
                    + SET_TEXT_COLOR_MAGENTA + " - Show available games.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "join <ID> [WHITE|BLACK]"
                    + SET_TEXT_COLOR_MAGENTA + " - Join a game as your choice of color.\n" +
                    "  " + SET_TEXT_COLOR_BLUE + "observe <ID>"
                    + SET_TEXT_COLOR_MAGENTA + " - Join a game as an observer." + SET_TEXT_COLOR_WHITE);
        }
    }
    private static String Register(String[] commands){
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
                    authState = LOGGED_IN;
                    return "Registered " + registerResponse.username() + "\nWelcome to Chess!";
                }
                else{
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            }
            catch (Exception e){
                return e.getMessage();
            }
        else{
            return "Must provide a Username, Password, and Email.";
        }
    }

    private static String Login(String[] commands){
        if (commands.length >= 3)
            try{
                UserData newUser = new UserData(commands[1], null, commands[2]);
                var jsonBody = new Gson().toJson(newUser);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(serverUrl + "/session"))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .header("Content-Type", "application/json")
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
                    myAuthToken = registerResponse.authToken();
                    authState = LOGGED_IN;
                    return "Logged in as " + registerResponse.username() + "\nWelcome to Chess!";
                }
                else{
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            }
            catch (Exception e){
                return e.getMessage();
            }
        else{
            return "Must provide a Username and Password.";
        }
    }

    private static String Logout(){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(serverUrl + "/session"))
                    .DELETE()
                    .header("Content-Type", "application/json")
                    .header("authorization", myAuthToken)
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                myAuthToken = "";
                authState = LOGGED_OUT;
                return "Logged out";
            }
            else{
                ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                myAuthToken = "";
                authState = LOGGED_OUT;
                return errorMessage.message();
            }
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

    private static String CreateGame(String[] commands){
        if (commands.length >= 2)
            try{
                StringBuilder fullNameBuilder = new StringBuilder();
                fullNameBuilder.append(commands[1]);
                for (int i = 2; i < commands.length; i++){
                    fullNameBuilder.append(" ");
                    fullNameBuilder.append(commands[i]);
                }
                String fullName = fullNameBuilder.toString();
                GameData newGame = new GameData(null, null, null, fullName, null);
                var jsonBody = new Gson().toJson(newGame);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(serverUrl + "/game"))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .header("Content-Type", "application/json")
                        .header("authorization", myAuthToken)
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    CreateGameResponse createGameResponse = new Gson().fromJson(response.body(), CreateGameResponse.class);
                    return "Created game with ID of " + createGameResponse.gameID();
                }
                else{
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            }
            catch (Exception e){
                return e.getMessage();
            }
        else{
            return "Must provide a Name.";
        }
    }

    public static String ListGames(){
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(serverUrl + "/game"))
                    .GET()
                    .header("Content-Type", "application/json")
                    .header("authorization", myAuthToken)
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                GamesListResponse gamesListResponse = new Gson().fromJson(response.body(), GamesListResponse.class);
                StringBuilder allGamesListStringBuilder = new StringBuilder();
                IDMap.clear();
                for (int i = 0; i < gamesListResponse.games().length; i++) {
                    var jsonBody = new Gson().toJson(gamesListResponse.games()[i]);
                    GameResponse gameResponse = new Gson().fromJson(jsonBody, GameResponse.class);
                    allGamesListStringBuilder
                            .append(SET_TEXT_COLOR_YELLOW)
                            .append("#")
                            .append(i + 1)
                            .append(": ")
                            .append(SET_TEXT_COLOR_BLUE)
                            .append("Name: ")
                            .append(SET_TEXT_COLOR_MAGENTA)
                            .append(gameResponse.gameName())
                            .append(SET_TEXT_COLOR_BLUE)
                            .append(" White:")
                            .append(SET_TEXT_COLOR_MAGENTA)
                            .append(gameResponse.whiteUsername())
                            .append(SET_TEXT_COLOR_BLUE)
                            .append(" Black:")
                            .append(SET_TEXT_COLOR_MAGENTA)
                            .append(gameResponse.blackUsername())
                            .append("\n");
                    IDMap.put(i+1, gameResponse.gameID());
                }
                return allGamesListStringBuilder.toString();
            }
            else{
                ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                return errorMessage.message();
            }
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

    private static String JoinGame(String[] commands){
        if (commands.length >= 3)
            try{
                Integer localGameID = Integer.parseInt(commands[1]);
                Integer remoteGameID = IDMap.get(localGameID);
                String playerColor = commands[2].toUpperCase();
                JoinGameRequest newGame = new JoinGameRequest(playerColor, remoteGameID);
                var jsonBody = new Gson().toJson(newGame);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(serverUrl + "/game"))
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .header("Content-Type", "application/json")
                        .header("authorization", myAuthToken)
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return "Joined game";
                }
                else{
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            }
            catch (Exception e){
                return e.getMessage();
            }
        else{
            return "Must provide a GameID and Color.";
        }
    }
}