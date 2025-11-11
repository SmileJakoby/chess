package client;

import com.google.gson.Gson;
import datamodel.LoginResponse;
import datamodel.RegisterResponse;
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
        UserData request = new UserData("player1", "password", "p1@email.com");
        var response = facade.Register(request);
        RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
        assertNotNull(registerResponse.authToken());
        assertTrue(registerResponse.authToken().length() > 10);
    }
    @Test
    @Order(2)
    void registerNegative() throws Exception {
        UserData request = new UserData("player1", "password", "p1@email.com");
        facade.Register(request);
        var response = facade.Register(request);
        assertEquals(403, response.statusCode());
    }
    @Test
    @Order(3)
    void loginPositive() throws Exception {
        UserData request = new UserData("player1", "password", "p1@email.com");
        var response = facade.Register(request);
        request = new UserData("player1", "password", "p1@email.com");
        response = facade.Login(request);
        LoginResponse loginResponse = new Gson().fromJson(response.body(), LoginResponse.class);
        assertNotNull(loginResponse.authToken());
        assertTrue(loginResponse.authToken().length() > 10);
    }

    @Test
    @Order(4)
    void loginNegative() throws Exception {
        var request = new UserData("NotAUser", "password", "p1@email.com");
        var response = facade.Login(request);
        assertEquals(401, response.statusCode());
    }


}
