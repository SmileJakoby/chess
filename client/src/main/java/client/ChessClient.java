package client;

import ClientSideDataModel.*;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import ClientSideDataModel.*;
import model.GameData;
import model.UserData;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static ui.EscapeSequences.*;

import java.net.http.HttpResponse;


public class ChessClient {

    private static final int LOGGED_OUT = 0;
    private static final int LOGGED_IN = 1;
    private static final ServerFacade SERVER_FACADE = new ServerFacade();
    private static boolean hasListed = false;

    private static String myAuthToken = "";

    private static int authState = LOGGED_OUT;
    private static String myUserName = "";

    private static final Map<Integer,Integer> ID_MAP = new HashMap<>();
    private static final Map<Integer, Boolean> IS_BLACK_MAP = new HashMap<>();

    public ChessClient(String givenServerUrl){
        SERVER_FACADE.setServerURL(givenServerUrl);
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


            try {
                result = evalCLI(line);
                System.out.print(SET_TEXT_COLOR_YELLOW + result + "\n" + SET_TEXT_COLOR_WHITE);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    private static String evalCLI(String givenString)
    {
        var commands = givenString.split(" ");
        commands[0] = commands[0].toLowerCase();
        if (authState == LOGGED_OUT) {
            switch (commands[0]) {
                case "help":
                    return printHelp();
                case "register":
                    return register(commands);
                case "login":
                    return login(commands);
                case "quit":
                    return "Goodbye!";
            }
        }
        if (authState == LOGGED_IN) {
            switch (commands[0]) {
                case "help":
                    return printHelp();
                case "logout":
                    return logout();
                case "create":
                    return createGame(commands);
                case "list":
                    return listGames();
                case "join":
                    return joinGame(commands);
                case "observe":
                    return observeGame(commands);
            }
        }
        return "Unrecognized command";
    }
    private static String printHelp(){
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
    private static String register(String[] commands){
        if (commands.length >= 4) {
            try {
                UserData newUser = new UserData(commands[1], commands[3], commands[2]);
                HttpResponse<String> response = SERVER_FACADE.register(newUser);
                if (response.statusCode() == 200) {
                    RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
                    myAuthToken = registerResponse.authToken();
                    authState = LOGGED_IN;
                    myUserName = registerResponse.username();
                    return "Registered " + registerResponse.username() + "\nWelcome to Chess!";
                } else {
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        else{
            return "Must provide a Username, Password, and Email.";
        }
    }

    private static String login(String[] commands){
        if (commands.length >= 3) {
            try {
                UserData newUser = new UserData(commands[1], null, commands[2]);
                HttpResponse<String> response = SERVER_FACADE.login(newUser);
                if (response.statusCode() == 200) {
                    RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
                    myAuthToken = registerResponse.authToken();
                    authState = LOGGED_IN;
                    myUserName = registerResponse.username();
                    return "Logged in as " + registerResponse.username() + "\nWelcome to Chess!";
                } else {
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        else{
            return "Must provide a Username and Password.";
        }
    }

    private static String logout(){
        try{
            HttpResponse<String> response = SERVER_FACADE.logout(myAuthToken);
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

    private static String createGame(String[] commands){
        if (commands.length >= 2) {
            try {
                StringBuilder fullNameBuilder = new StringBuilder();
                fullNameBuilder.append(commands[1]);
                for (int i = 2; i < commands.length; i++) {
                    fullNameBuilder.append(" ");
                    fullNameBuilder.append(commands[i]);
                }
                String fullName = fullNameBuilder.toString();
                GameData newGame = new GameData(null, null, null, fullName, null);
                HttpResponse<String> response = SERVER_FACADE.createGame(newGame, myAuthToken);
                if (response.statusCode() == 200) {
                    CreateGameResponse createGameResponse = new Gson().fromJson(response.body(), CreateGameResponse.class);
                    return "Created game with ID of " + createGameResponse.gameID();
                } else {
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        else{
            return "Must provide a Name.";
        }
    }

    public static String listGames(){
        try{
            HttpResponse<String> response = SERVER_FACADE.listGames(myAuthToken);
            if (response.statusCode() == 200) {
                GamesListResponse gamesListResponse = new Gson().fromJson(response.body(), GamesListResponse.class);
                StringBuilder allGamesListStringBuilder = new StringBuilder();
                ID_MAP.clear();
                IS_BLACK_MAP.clear();
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
                    ID_MAP.put(i+1, gameResponse.gameID());
                    if (gameResponse.blackUsername() != null &&gameResponse.blackUsername().equals(myUserName)) {
                        IS_BLACK_MAP.put(i+1, true);
                    }
                    else{
                        IS_BLACK_MAP.put(i+1, false);
                    }
                }
                hasListed = true;
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

    private static String joinGame(String[] commands){
        if (!hasListed){
            return "Unknown GameID. Make sure to use the command 'list' before trying to join or observe.";
        }
        if (commands.length >= 3) {
            try {
                Integer localGameID = Integer.parseInt(commands[1]);
                Integer remoteGameID = ID_MAP.get(localGameID);
                String playerColor = commands[2].toUpperCase();
                JoinGameRequest joinGameRequest = new JoinGameRequest(playerColor, remoteGameID);
                HttpResponse<String> response = SERVER_FACADE.joinGame(joinGameRequest, myAuthToken);
                if (response.statusCode() == 200) {
                    if (playerColor.equals("BLACK")) {
                        IS_BLACK_MAP.put(localGameID, true);
                    }
                    return "Joined game";
                } else {
                    ErrorMessage errorMessage = new Gson().fromJson(response.body(), ErrorMessage.class);
                    return errorMessage.message();
                }
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        else{
            return "Must provide a GameID and Color.";
        }
    }

    private static String observeGame(String[] commands){
        if (!hasListed){
            return "Unknown GameID. Make sure to use the command 'list' before trying to join or observe.";
        }
        if (commands.length >= 2) {
            try {
                //Observe game not fully implemented yet.
                //The server backend isn't even built to send a game
                //from the database yet.
                ChessGame newGame = new ChessGame();
                return chessGameDisplay(newGame, IS_BLACK_MAP.get(Integer.parseInt(commands[1])));
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        else{
            return "Must provide a GameID and Color.";
        }
    }

    private static String chessGameDisplay(ChessGame givenGame, Boolean isBlack){
        StringBuilder boardBuilder = new StringBuilder();
        String rowString = "    a  b  c  d  e  f  g  h    ";
        int iStartingValue = 8;
        int iStoppingValue = 0;
        int iIncrementer = -1;
        int jStartingValue = 1;
        int jStoppingValue = 9;
        int jIncrementer = 1;

        if (isBlack){
            rowString = "    h  g  f  e  d  c  b  a    ";
            jStartingValue = 8;
            jStoppingValue = 0;
            jIncrementer = -1;
            iStartingValue = 1;
            iStoppingValue = 9;
            iIncrementer = 1;
        }
        boardBuilder
                .append(SET_BG_COLOR_LIGHT_GREY)
                .append(SET_TEXT_COLOR_BLACK)
                .append(rowString)
                .append(RESET_BG_COLOR)
                .append("\n");

        for (int i = iStartingValue; i != iStoppingValue; i += iIncrementer) {
            boardBuilder
                    .append(SET_BG_COLOR_LIGHT_GREY)
                    .append(SET_TEXT_COLOR_BLACK)
                    .append(" ")
                    .append(i)
                    .append(" ")
                    .append(RESET_BG_COLOR);
            for (int j = jStartingValue; j != jStoppingValue; j += jIncrementer) {
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
                .append(rowString)
                .append(RESET_BG_COLOR)
                .append("\n");
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