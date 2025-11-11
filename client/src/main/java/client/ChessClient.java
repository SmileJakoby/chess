package client;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
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
    private static final ServerFacade serverFacade = new ServerFacade();

    private static String serverUrl = "";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static String myAuthToken = "";

    private static int authState = LOGGED_OUT;
    private static String myUserName = "";

    private static final Map<Integer,Integer> IDMap = new HashMap<>();
    private static final Map<Integer, Boolean> isBlackMap = new HashMap<>();

    public ChessClient(String givenServerUrl){
        serverUrl = givenServerUrl;
        serverFacade.setServerURL(givenServerUrl);
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
                    return ObserveGame(commands);
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
                HttpResponse<String> response = serverFacade.Register(newUser);
                if (response.statusCode() == 200) {
                    RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
                    myAuthToken = registerResponse.authToken();
                    authState = LOGGED_IN;
                    myUserName = registerResponse.username();
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
                HttpResponse<String> response = serverFacade.Login(newUser);
                if (response.statusCode() == 200) {
                    RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
                    myAuthToken = registerResponse.authToken();
                    authState = LOGGED_IN;
                    myUserName = registerResponse.username();
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
                myUserName = "";
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
                isBlackMap.clear();
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
                    if (gameResponse.blackUsername() != null &&gameResponse.blackUsername().equals(myUserName)) {
                        isBlackMap.put(i+1, true);
                    }
                    else{
                        isBlackMap.put(i+1, false);
                    }
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

    private static String ObserveGame(String[] commands){
        if (commands.length >= 2)
            try{
                //Observe game not fully implemented yet.
                //The server backend isn't even built to send a game
                //from the database yet.
                ChessGame newGame = new ChessGame();
                return chessGameDisplay(newGame, isBlackMap.get(Integer.parseInt(commands[1])));
            }
            catch (Exception e){
                return e.getMessage();
            }
        else{
            return "Must provide a GameID and Color.";
        }
    }

    private static String chessGameDisplay(ChessGame givenGame, Boolean isBlack){
        StringBuilder boardBuilder = new StringBuilder();
        if (!isBlack) {
            boardBuilder
                    .append(SET_BG_COLOR_LIGHT_GREY)
                    .append(SET_TEXT_COLOR_BLACK)
                    .append("    a  b  c  d  e  f  g  h    ")
                    .append(RESET_BG_COLOR)
                    .append("\n");

            for (int i = 8; i > 0; i--) {
                boardBuilder
                        .append(SET_BG_COLOR_LIGHT_GREY)
                        .append(SET_TEXT_COLOR_BLACK)
                        .append(" ")
                        .append(i)
                        .append(" ")
                        .append(RESET_BG_COLOR);
                for (int j = 1; j <= 8; j++) {
                    String squareColor = SET_BG_COLOR_WHITE;
                    if ((i + j) % 2 == 0) {
                        squareColor = SET_BG_COLOR_BLACK;
                    }
                    boardBuilder
                            .append(squareColor)
                            .append(" ")
                            .append(drawPiece(givenGame.getBoard().getPiece(new ChessPosition(i, j))))
                            .append(" ");
                }
                boardBuilder
                        .append(SET_BG_COLOR_LIGHT_GREY)
                        .append(SET_TEXT_COLOR_BLACK)
                        .append(" ")
                        .append(i)
                        .append(" ")
                        .append(RESET_BG_COLOR)
                        .append("\n");
            }
            boardBuilder
                    .append(SET_BG_COLOR_LIGHT_GREY)
                    .append(SET_TEXT_COLOR_BLACK)
                    .append("    a  b  c  d  e  f  g  h    ")
                    .append(RESET_BG_COLOR)
                    .append("\n");
        }
        else {
            if (isBlack) {
                boardBuilder
                        .append(SET_BG_COLOR_LIGHT_GREY)
                        .append(SET_TEXT_COLOR_BLACK)
                        .append("    h  g  f  e  d  c  b  a    ")
                        .append(RESET_BG_COLOR)
                        .append("\n");

                for (int i = 1; i <= 8; i++) {
                    boardBuilder
                            .append(SET_BG_COLOR_LIGHT_GREY)
                            .append(SET_TEXT_COLOR_BLACK)
                            .append(" ")
                            .append(i)
                            .append(" ")
                            .append(RESET_BG_COLOR);
                    for (int j = 8; j >= 1; j--) {
                        String squareColor = SET_BG_COLOR_WHITE;
                        if ((i + j) % 2 == 0) {
                            squareColor = SET_BG_COLOR_BLACK;
                        }
                        boardBuilder
                                .append(squareColor)
                                .append(" ")
                                .append(drawPiece(givenGame.getBoard().getPiece(new ChessPosition(i, j))))
                                .append(" ");
                    }
                    boardBuilder
                            .append(SET_BG_COLOR_LIGHT_GREY)
                            .append(SET_TEXT_COLOR_BLACK)
                            .append(" ")
                            .append(i)
                            .append(" ")
                            .append(RESET_BG_COLOR)
                            .append("\n");
                }
                boardBuilder
                        .append(SET_BG_COLOR_LIGHT_GREY)
                        .append(SET_TEXT_COLOR_BLACK)
                        .append("    h  g  f  e  d  c  b  a    ")
                        .append(RESET_BG_COLOR)
                        .append("\n");
            }
        }
        return boardBuilder.toString();
    }

    private static String drawPiece(ChessPiece givenPiece){
        if (givenPiece == null)
        {
            return " ";
        }
        switch (givenPiece.getPieceType()){
            case KING:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "K";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "K";
                }
            case QUEEN:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "Q";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "Q";
                }
            case BISHOP:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "B";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "B";
                }
            case KNIGHT:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "N";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "N";
                }
            case ROOK:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "R";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "R";
                }
            case PAWN:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "P";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "P";
                }
        }
        return EMPTY;
    }
}