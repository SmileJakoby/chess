package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceUnitTests {


    // ### TESTING SETUP/CLEANUP ###
    // ### SERVER-LEVEL API TESTS ###
    public static DataAccess dataAccess = new MemoryDataAccess();
    public static DatabaseService databaseService = new DatabaseService(dataAccess);
    public static UserService userService = new UserService(dataAccess);
    public static SessionService sessionService = new SessionService(dataAccess);
    public static GameService gameService = new GameService(dataAccess);
    @BeforeEach
    public void setup() {
        try{databaseService.clear();}
        catch(Exception ex){Assertions.fail(ex.getMessage());}
    }

    @Test
    @Order(1)
    @DisplayName("Clear Positive")
    public void clearPositive() {
        try {
            UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
            UserData user2 = new UserData("Luke", "luke@gmail.com", "98765");
            dataAccess.addUser(user1);
            dataAccess.addUser(user2);
            AuthData auth1 = new AuthData("qwerty", "Jacob");
            AuthData auth2 = new AuthData("asdf", "Luke");
            dataAccess.addAuthData(auth1);
            dataAccess.addAuthData(auth2);
            GameData game1 = new GameData(1, "Jacob", "Luke", "1v1 me bro", new ChessGame());
            GameData game2 = new GameData(2, "Jacob", "Luke", "Rematch", new ChessGame());
            dataAccess.addGame(game1);
            dataAccess.addGame(game2);
            Assertions.assertEquals(user1.username(), dataAccess.getUser(user1.username()).username(),
                    "dataAccess failed, couldn't add user");
            Assertions.assertEquals(user2.username(), dataAccess.getUser(user2.username()).username(),
                    "dataAccess failed, couldn't add user");
            Assertions.assertEquals(auth1.authToken(), dataAccess.getAuthData(auth1.authToken()).authToken(),
                    "dataAccess failed, couldn't add auth");
            Assertions.assertEquals(auth2.authToken(), dataAccess.getAuthData(auth2.authToken()).authToken(),
                    "dataAccess failed, couldn't add auth");
            Assertions.assertEquals(game1.gameID(), dataAccess.getGame(game1.gameID()).gameID(),
                    "dataAccess failed, couldn't add game");
            Assertions.assertEquals(game2.gameID(), dataAccess.getGame(game2.gameID()).gameID(),
                    "dataAccess failed, couldn't add game");
            try {
                databaseService.clear();
            } catch (Exception ex) {
                Assertions.fail(ex.getMessage());
            }
            Assertions.assertNull(dataAccess.getUser(user1.username()));
            Assertions.assertNull(dataAccess.getUser(user2.username()));
            Assertions.assertNull(dataAccess.getAuthData(auth1.authToken()));
            Assertions.assertNull(dataAccess.getAuthData(auth2.authToken()));
            Assertions.assertNull(dataAccess.getGame(game1.gameID()));
            Assertions.assertNull(dataAccess.getGame(game2.gameID()));
        }
        catch (DataAccessException ex){
            Assertions.fail(ex.getMessage());
        }
    }
    @Test
    @Order(2)
    @DisplayName("Register Positive")
    public void registerPositive() throws Exception {
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        RegisterResponse response = userService.register(user1);
        Assertions.assertNotNull(response, "Did not get response");
        Assertions.assertEquals(user1.username(), response.username(), "response username mismatch");
        Assertions.assertEquals(user1.username(), dataAccess.getUser(user1.username()).username(), "database username mismatch");
    }
    @Test
    @Order(3)
    @DisplayName("Register Negative")
    public void registerNegative() {
        UserData user1 = new UserData("Jacob",null,"12345");
        Assertions.assertThrows(BadRequestException.class, () -> {userService.register(user1);});
    }

    @Test
    @Order(4)
    @DisplayName("Login Positive")
    public void loginPositive() throws Exception{
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        dataAccess.addUser(user1);
        LoginResponse response = sessionService.login(user1);
        Assertions.assertNotNull(response, "Did not get response");
        Assertions.assertEquals(user1.username(), response.username(), "response username mismatch");
        Assertions.assertEquals(response.authToken(), dataAccess.getAuthData(response.authToken()).authToken(), "response authToken mismatch");
    }

    @Test
    @Order(5)
    @DisplayName("Login Negative")
    public void loginNegative() {
        try {
            UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
            dataAccess.addUser(user1);
            Assertions.assertThrows(UnauthorizedException.class, () -> {
                UserData badLogin = new UserData("Jacob", "jacobskarda@gmail.com", "Totally my password");
                sessionService.login(badLogin);
            });
        }
        catch (DataAccessException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Logout Positive")
    public void logoutPositive() throws Exception{
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        dataAccess.addUser(user1);
        AuthData auth1 = new AuthData("qwerty", "Jacob");
        dataAccess.addAuthData(auth1);
        sessionService.logout(auth1);
        Assertions.assertNull(dataAccess.getAuthData(auth1.authToken()), "AuthData was not removed from database");
    }

    @Test
    @Order(7)
    @DisplayName("Logout Negative")
    public void logoutNegative() {
        try {
            UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
            dataAccess.addUser(user1);
            AuthData auth1 = new AuthData("qwerty", "Jacob");
            Assertions.assertThrows(UnauthorizedException.class, () -> {
                sessionService.logout(auth1);
            });
        }
        catch (DataAccessException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    @Order(8)
    @DisplayName("List Games Positive")
    public void listGamesPositive() throws Exception{
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        UserData user2 = new UserData("Luke","luke@gmail.com","98765");
        dataAccess.addUser(user1);
        dataAccess.addUser(user2);
        AuthData auth1 = new AuthData("qwerty", "Jacob");
        AuthData auth2 = new AuthData("asdf", "Luke");
        dataAccess.addAuthData(auth1);
        dataAccess.addAuthData(auth2);
        GameData game1 = new GameData(1,"Jacob","Luke","1v1 me bro",new ChessGame());
        GameData game2 = new GameData(2,"Jacob","Luke","Rematch",new ChessGame());
        dataAccess.addGame(game1);
        dataAccess.addGame(game2);
        GamesListResponse response = gameService.getGamesList(auth1);
        Assertions.assertNotNull(response, "GamesList was null");
    }

    @Test
    @Order(9)
    @DisplayName("List Games Negative")
    public void listGamesNegative() {
        try {
            UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
            UserData user2 = new UserData("Luke", "luke@gmail.com", "98765");
            dataAccess.addUser(user1);
            dataAccess.addUser(user2);
            AuthData auth1 = new AuthData("I am not authorized", "Jacob");
            GameData game1 = new GameData(1, "Jacob", "Luke", "1v1 me bro", new ChessGame());
            GameData game2 = new GameData(2, "Jacob", "Luke", "Rematch", new ChessGame());
            dataAccess.addGame(game1);
            dataAccess.addGame(game2);
            Assertions.assertThrows(UnauthorizedException.class, () -> {
                gameService.getGamesList(auth1);
            });
        }
        catch (DataAccessException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    @Order(10)
    @DisplayName("Create Game Positive")
    public void createGamePositive() throws Exception{
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        dataAccess.addUser(user1);
        AuthData auth1 = new AuthData("qwerty", "Jacob");
        dataAccess.addAuthData(auth1);

        CreateGameResponse response = gameService.createGame(auth1, "The final showdown");

        Assertions.assertNotNull(response, "Did not get response");
        Assertions.assertEquals("The final showdown", dataAccess.getGame(response.gameID()).gameName(), "game name did not match/wasn't found");
    }

    @Test
    @Order(11)
    @DisplayName("Create Game Negative")
    public void createGameNegative() {
        try {
            UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
            dataAccess.addUser(user1);
            AuthData auth1 = new AuthData("I am not authorized", "Jacob");
            Assertions.assertThrows(UnauthorizedException.class, () -> {
                gameService.createGame(auth1, "illegal game");
            });
        }
        catch (DataAccessException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    @Order(12)
    @DisplayName("Join Positive")
    public void joinPositive() throws Exception{
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        UserData user2 = new UserData("Luke","luke@gmail.com","98765");
        dataAccess.addUser(user1);
        dataAccess.addUser(user2);
        AuthData auth1 = new AuthData("qwerty", "Jacob");
        AuthData auth2 = new AuthData("asdf", "Luke");
        dataAccess.addAuthData(auth1);
        dataAccess.addAuthData(auth2);
        GameData game1 = new GameData(1,"Jacob",null,"1v1 me bro",new ChessGame());
        dataAccess.addGame(game1);
        gameService.joinGame(auth1, "BLACK", 1);
        Assertions.assertEquals(user1.username(), dataAccess.getGame(1).blackUsername(), "black player username mismatch");
    }

    @Test
    @Order(13)
    @DisplayName("Join Game Negative")
    public void joinGameNegative() {
        try {
            UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
            UserData user2 = new UserData("Luke", "luke@gmail.com", "98765");
            dataAccess.addUser(user1);
            dataAccess.addUser(user2);
            AuthData auth1 = new AuthData("qwerty", "Jacob");
            AuthData auth2 = new AuthData("asdf", "Luke");
            dataAccess.addAuthData(auth1);
            dataAccess.addAuthData(auth2);
            GameData game1 = new GameData(1, "Jacob", null, "1v1 me bro", new ChessGame());
            dataAccess.addGame(game1);
            Assertions.assertThrows(AlreadyTakenException.class, () -> {
                gameService.joinGame(auth1, "WHITE", 1);
            });
        }
        catch (DataAccessException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    @Order(14)
    @DisplayName("Make Move")
    public void makeMove() throws Exception {
        joinPositive();
        GameData originalGameData = dataAccess.getGame(1);
        originalGameData.game().makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));
        dataAccess.updateGame(1, originalGameData);
        Assertions.assertEquals(originalGameData, dataAccess.getGame(1));
    }
}