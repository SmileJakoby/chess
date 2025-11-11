package client;

import com.google.gson.Gson;
import datamodel.RegisterResponse;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade();
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("http://localhost:")
                .append(port);
        facade.setServerURL(urlBuilder.toString());
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
    void registerPositive() throws Exception {
        UserData request = new UserData("player1", "password", "p1@email.com");
        var response = facade.Register(request);
        RegisterResponse registerResponse = new Gson().fromJson(response.body(), RegisterResponse.class);
        assertNotNull(registerResponse.authToken());
        assertTrue(registerResponse.authToken().length() > 10);
    }
}
