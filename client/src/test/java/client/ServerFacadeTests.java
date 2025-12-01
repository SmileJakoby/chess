package client;

import clientsidedatamodel.JoinGameRequest;
import com.google.gson.Gson;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade();
        String urlBuilder = "http://localhost:" + port;
        facade.setServerURL(urlBuilder);
        try {
            facade.clearDatabase();
        }
        catch (Exception e) {
            System.out.println("Clear database failed. " + e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    void registerPositive() throws Exception {
        UserData request = new UserData("registerPlayer", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        assertNotNull(registerResponse.authToken());
        assertTrue(registerResponse.authToken().length() > 10);
    }
    @Test
    @Order(2)
    void registerNegative() throws Exception {
        UserData request = new UserData("registerPlayer2", "password", "p1@email.com");
        facade.register(request);
        var response = facade.register(request);
        assertEquals(403, response.statusCode());
    }
    @Test
    @Order(3)
    void loginPositive() throws Exception {
        UserData request = new UserData("loginPlayer", "password", "p1@email.com");
        var response = facade.register(request);
        request = new UserData("loginPlayer", "password", "p1@email.com");
        response = facade.login(request);
        clientsidedatamodel.LoginResponse loginResponse = new Gson().fromJson(response.body(), clientsidedatamodel.LoginResponse.class);
        assertNotNull(loginResponse.authToken());
        assertTrue(loginResponse.authToken().length() > 10);
    }

    @Test
    @Order(4)
    void loginNegative() throws Exception {
        var request = new UserData("NotAUser", "password", "p1@email.com");
        var response = facade.login(request);
        assertEquals(401, response.statusCode());
    }

    @Test
    @Order(5)
    void logoutPositive() throws Exception {
        UserData request = new UserData("logoutPlayer", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        var response2 = facade.logout(registerResponse.authToken());
        assertNotNull(response2);
        assertEquals(200, response2.statusCode());
    }

    @Test
    @Order(6)
    void logoutNegative() throws Exception {
        var response = facade.logout("bad auth");
        assertEquals(401, response.statusCode());
    }
    @Test
    @Order(7)
    void createGamePositive() throws Exception {
        UserData request = new UserData("createGamePlayer", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        GameData gameData = new GameData(null,null,null,"It'sChessTime",null);
        var response2 = facade.createGame(gameData, registerResponse.authToken());
        assertNotNull(response2);
        assertEquals(200, response2.statusCode());
    }

    @Test
    @Order(8)
    void createGameNegative() throws Exception {
        UserData request = new UserData("createGamePlayer2", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        GameData gameData = new GameData(null,null,null,"It'sNotChessTime",null);
        facade.createGame(gameData, registerResponse.authToken());
        var response2 = facade.createGame(gameData, registerResponse.authToken());
        assertEquals(400, response2.statusCode());
    }

    @Test
    @Order(9)
    void listGamesPositive() throws Exception {
        UserData request = new UserData("listGamePlayer", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        GameData gameData1 = new GameData(null,null,null,"FirstGame",null);
        facade.createGame(gameData1, registerResponse.authToken());
        GameData gameData2 = new GameData(null,null,null,"SecondGame",null);
        facade.createGame(gameData2, registerResponse.authToken());
        GameData gameData3 = new GameData(null,null,null,"ThirdGame",null);
        facade.createGame(gameData3, registerResponse.authToken());
        var response2 = facade.listGames(registerResponse.authToken());
        assertEquals(200, response2.statusCode());
        clientsidedatamodel.GamesListResponse gamesListResponse = new Gson().fromJson(response2.body(), clientsidedatamodel.GamesListResponse.class);
        assertTrue(gamesListResponse.games().length >= 3);
    }
    @Test
    @Order(10)
    void listGamesNegative() throws Exception {
        var response = facade.listGames("bad auth");
        assertEquals(401, response.statusCode());
    }

    @Test
    @Order(11)
    void joinPositive() throws Exception {
        UserData request = new UserData("joinGamePlayer", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        GameData gameData1 = new GameData(null,null,null,"JoinGame",null);
        var response2 = facade.createGame(gameData1, registerResponse.authToken());
        assertNotNull(response2);
        clientsidedatamodel.CreateGameResponse createGameResponse = new Gson().fromJson(response2.body(), clientsidedatamodel.CreateGameResponse.class);
        clientsidedatamodel.JoinGameRequest joinGameRequest = new clientsidedatamodel.JoinGameRequest("WHITE",createGameResponse.gameID());
        var response3 = facade.joinGame(joinGameRequest, registerResponse.authToken());
        assertEquals(200, response3.statusCode());
    }

    @Test
    @Order(12)
    void joinNegative() throws Exception {
        UserData request = new UserData("joinGamePlayer2", "password", "p1@email.com");
        var response = facade.register(request);
        clientsidedatamodel.RegisterResponse registerResponse = new Gson().fromJson(response.body(), clientsidedatamodel.RegisterResponse.class);
        GameData gameData1 = new GameData(null,null,null,"JoinGame2",null);
        var response2 = facade.createGame(gameData1, registerResponse.authToken());
        clientsidedatamodel.CreateGameResponse createGameResponse = new Gson().fromJson(response2.body(), clientsidedatamodel.CreateGameResponse.class);
        clientsidedatamodel.JoinGameRequest joinGameRequest = new JoinGameRequest("WHITE",createGameResponse.gameID());
        facade.joinGame(joinGameRequest, registerResponse.authToken());
        var response3 = facade.joinGame(joinGameRequest, registerResponse.authToken());
        assertEquals(403, response3.statusCode());
    }
}
