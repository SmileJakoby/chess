package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import service.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessUnitTests {


    // ### TESTING SETUP/CLEANUP ###
    // ### SERVER-LEVEL API TESTS ###
    public static DataAccess dataAccess = new SQLDataAccess();
    @BeforeEach
    public void setup() {
        try{dataAccess.clear();}
        catch(Exception ex){Assertions.fail(ex.getMessage());}
    }

    //Clear

    //GetGameCount
    //GetGameByName
    //UpdateGame


    @Test
    @Order(1)
    @DisplayName("Add User Positive DAO")
    public void addUserPositiveDAO() throws DataAccessException {
        UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
        UserData user2 = new UserData("Luke", "luke@gmail.com", "98765");
        dataAccess.addUser(user1);
        dataAccess.addUser(user2);
        Assertions.assertEquals(user1.username(), dataAccess.getUser(user1.username()).username(),
                "dataAccess failed, couldn't add user");
        Assertions.assertEquals(user2.username(), dataAccess.getUser(user2.username()).username(),
                "dataAccess failed, couldn't add user");
    }
    @Test
    @Order(2)
    @DisplayName("Add User Negative DAO")
    public void addUserNegativeDAO() throws DataAccessException {
        UserData user1 = new UserData("Jacob", "jacobskarda@gmail.com", "12345");
        UserData user2 = new UserData("Jacob", "luke@gmail.com", "98765");
        dataAccess.addUser(user1);
        Assertions.assertThrows(DataAccessException.class, () -> {dataAccess.addUser(user2);});
    }
    @Test
    @Order(3)
    @DisplayName("Get User Positive DAO")
    public void getUserPositiveDAO() throws DataAccessException {
        addUserPositiveDAO();
        UserData insertedUser = dataAccess.getUser("Jacob");
        Assertions.assertEquals("Jacob", insertedUser.username(),
                "dataAccess failed, couldn't add user");
    }
    @Test
    @Order(4)
    @DisplayName("Get User Negative DAO")
    public void getUserNegativeDAO() throws DataAccessException {
        addUserPositiveDAO();
        UserData insertedUser = dataAccess.getUser("Emily");
        Assertions.assertNull(insertedUser, "Emily should not exist, nothing should have been returned");
    }

    @Test
    @Order(5)
    @DisplayName("Add Auth Positive DAO")
    public void addAuthPositiveDAO() throws DataAccessException {
        AuthData auth1 = new AuthData("JacobAuthorized", "Jacob");
        AuthData auth2 = new AuthData("LukeAuthorized", "Luke");
        dataAccess.addAuthData(auth1);
        dataAccess.addAuthData(auth2);
        Assertions.assertEquals(auth1.authToken(), dataAccess.getAuthData(auth1.authToken()).authToken(),
                "dataAccess failed, couldn't add auth 1");
        Assertions.assertEquals(auth2.authToken(), dataAccess.getAuthData(auth2.authToken()).authToken(),
                "dataAccess failed, couldn't add auth 2");
    }
    @Test
    @Order(6)
    @DisplayName("Add Auth Negative DAO")
    public void addAuthNegativeDAO() throws DataAccessException {
        AuthData auth1 = new AuthData("JacobAuthorized", "Jacob");
        AuthData auth2 = new AuthData("JacobAuthorized", "Luke");
        dataAccess.addAuthData(auth1);
        Assertions.assertThrows(DataAccessException.class, () -> {dataAccess.addAuthData(auth2);});
    }

    @Test
    @Order(7)
    @DisplayName("Get Auth Positive DAO")
    public void getAuthPositiveDAO() throws DataAccessException {
        addAuthPositiveDAO();
        AuthData insertedAuth = dataAccess.getAuthData("JacobAuthorized");
        Assertions.assertEquals("JacobAuthorized", insertedAuth.authToken(),
                "dataAccess failed, couldn't add auth");
    }
    @Test
    @Order(8)
    @DisplayName("Get Auth Negative DAO")
    public void getAuthNegativeDAO() throws DataAccessException {
        addAuthPositiveDAO();
        AuthData insertedAuth = dataAccess.getAuthData("EmilyAuthorized");
        Assertions.assertNull(insertedAuth, "Emily should not exist, nothing should have been returned");
    }

    @Test
    @Order(9)
    @DisplayName("Remove Auth Positive DAO")
    public void removeAuthPositiveDAO() throws DataAccessException {
        addAuthPositiveDAO();
        dataAccess.removeAuthData("JacobAuthorized");
        AuthData insertedAuth = dataAccess.getAuthData("JacobAuthorized");
        Assertions.assertNull(insertedAuth,
                "JacobAuthorized should not exist");
    }
    @Test
    @Order(10)
    @DisplayName("Remove Auth Negative DAO")
    public void removeAuthNegativeDAO() throws DataAccessException {
        //This isn't necessary... I don't care if you try to remove something that doesn't exist.
    }

    @Test
    @Order(11)
    @DisplayName("Add Game Positive DAO")
    public void addGamePositiveDAO() throws DataAccessException {
        GameData game1 = new GameData(null, "Jacob", "Luke", "1v1 me bro", new ChessGame());
        GameData game2 = new GameData(null, "Jacob", "Luke", "Rematch", new ChessGame());
        int gameID1 = dataAccess.addGame(game1);
        int gameID2 = dataAccess.addGame(game2);
        Assertions.assertEquals(gameID1, dataAccess.getGame(gameID1).gameID(),
                "dataAccess failed, couldn't add game");
        Assertions.assertEquals(gameID2, dataAccess.getGame(gameID2).gameID(),
                "dataAccess failed, couldn't add game");
    }
    @Test
    @Order(12)
    @DisplayName("Add Game Negative DAO")
    public void addGameNegativeDAO() throws DataAccessException {
        GameData game1 = new GameData(null, "Jacob", "Luke", null, new ChessGame());
        Assertions.assertThrows(DataAccessException.class, () -> {dataAccess.addGame(game1);});
    }

    @Test
    @Order(13)
    @DisplayName("Get Game Positive DAO")
    public void getGamePositiveDAO() throws DataAccessException {
        GameData game1 = new GameData(null, "Jacob", "Luke", "1v1 me bro", new ChessGame());
        int gameID1 = dataAccess.addGame(game1);
        GameData insertedGame = dataAccess.getGame(gameID1);
        Assertions.assertEquals(game1.game(), insertedGame.game(),
                "dataAccess failed, couldn't get game");
    }
    @Test
    @Order(14)
    @DisplayName("Get Game Negative DAO")
    public void getGameNegativeDAO() throws DataAccessException {
        addGamePositiveDAO();
        GameData insertedGame = dataAccess.getGame(999999999);
        Assertions.assertNull(insertedGame, "Emily should not exist, nothing should have been returned");
    }
    @Test
    @Order(15)
    @DisplayName("Clear Positive DAO")
    public void clearPositiveDAO() {
        try {
            addGamePositiveDAO();
            addUserPositiveDAO();
            addAuthPositiveDAO();
            dataAccess.clear();
            Assertions.assertNull(dataAccess.getUser("Jacob"));
            Assertions.assertNull(dataAccess.getUser("Luke"));
            Assertions.assertNull(dataAccess.getAuthData("JacobAuthorized"));
            Assertions.assertNull(dataAccess.getAuthData("LukeAuthorized"));
            Assertions.assertNull(dataAccess.getGame(1));
            Assertions.assertNull(dataAccess.getGame(2));
        }
        catch (DataAccessException ex) {
            Assertions.fail(ex.getMessage());
        }
    }
    @Test
    @Order(16)
    @DisplayName("Get GameList Positive DAO")
    public void getGameListPositiveDAO() throws DataAccessException {
        addGamePositiveDAO();
        GameData[] gamesList = dataAccess.getGameDataList();
        Assertions.assertNotNull(gamesList, "GamesList was null");
    }

    @Test
    @Order(17)
    @DisplayName("Get GameList Negative DAO")
    public void getGameListNegativeDAO() throws DataAccessException {
        clearPositiveDAO();
        GameData[] gamesList = dataAccess.getGameDataList();
        Assertions.assertEquals(0, gamesList.length, "GamesList was not empty");
    }

    @Test
    @Order(18)
    @DisplayName("Add Player Positive DAO")
    public void addPlayerPositiveDAO() throws DataAccessException {
        GameData game1 = new GameData(null, "Jacob", null, "1v1 me bro", new ChessGame());
        int gameID1 = dataAccess.addGame(game1);
        dataAccess.addPlayer(gameID1, "Luke", "BLACK");
        Assertions.assertEquals("Jacob", dataAccess.getGame(gameID1).whiteUsername(),
                "dataAccess failed, whiteUsername was overwritten");
        Assertions.assertEquals("Luke", dataAccess.getGame(gameID1).blackUsername(),
                "dataAccess failed, whiteUsername was overwritten");
    }
    @Test
    @Order(19)
    @DisplayName("Add Player Negative DAO")
    public void addPlayerNegativeDAO() throws DataAccessException {
        GameData game1 = new GameData(null, "Jacob", null, "1v1 me bro", new ChessGame());
        int gameID1 = dataAccess.addGame(game1);
        dataAccess.addPlayer(gameID1, "Luke", "GREEN");
        Assertions.assertEquals("Jacob", dataAccess.getGame(gameID1).whiteUsername(),
                "dataAccess failed, whiteUsername was overwritten");
        Assertions.assertNull(dataAccess.getGame(gameID1).blackUsername(),
                "dataAccess failed, somehow black player was added");
    }

    @Test
    @Order(20)
    @DisplayName("Update Game")
    public void updateGame() throws Exception {
        GameData game1 = new GameData(null, "Jacob", "Luke", "1v1 me bro", new ChessGame());
        int gameID1 = dataAccess.addGame(game1);
        GameData originalGameData = dataAccess.getGame(gameID1);
        originalGameData.game().makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));
        dataAccess.updateGame(gameID1, originalGameData);
        Assertions.assertEquals(originalGameData, dataAccess.getGame(gameID1));
    }
}