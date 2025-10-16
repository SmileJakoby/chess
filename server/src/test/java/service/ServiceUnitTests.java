package service;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import datamodel.RegisterResponse;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import passoff.model.TestAuthResult;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceUnitTests {


    // ### TESTING SETUP/CLEANUP ###
    // ### SERVER-LEVEL API TESTS ###
    public static DataAccess dataAccess = new MemoryDataAccess();
    public static DatabaseService databaseService = new DatabaseService(dataAccess);
    public static UserService userService = new UserService(dataAccess);
    public static SessionService sessionService = new SessionService(dataAccess);
    @BeforeEach
    public void setup() {
        dataAccess.clear();
    }

    @Test
    @Order(1)
    @DisplayName("Clear Positive")
    public void clearPositive() {
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        UserData user2 = new UserData("Luke","luke@gmail.com","98765");
        dataAccess.addUser(user1);
        dataAccess.addUser(user2);
        AuthData auth1 = new AuthData("qwerty", "Jacob");
        AuthData auth2 = new AuthData("asdf", "Luke");
        dataAccess.addAuthData(auth1);
        dataAccess.addAuthData(auth2);
        Assertions.assertEquals(user1.username(), dataAccess.getUser(user1.username()).username(),
                "dataAccess failed, couldn't add user");
        Assertions.assertEquals(user2.username(), dataAccess.getUser(user2.username()).username(),
                "dataAccess failed, couldn't add user");
        Assertions.assertEquals(auth1.authToken(), dataAccess.getAuthData(auth1.authToken()).authToken(),
                "dataAccess failed, couldn't add auth");
        Assertions.assertEquals(auth2.authToken(), dataAccess.getAuthData(auth2.authToken()).authToken(),
                "dataAccess failed, couldn't add auth");
        databaseService.clear();
        Assertions.assertNull(dataAccess.getUser(user1.username()));
        Assertions.assertNull(dataAccess.getUser(user2.username()));
        Assertions.assertNull(dataAccess.getAuthData(auth1.authToken()));
        Assertions.assertNull(dataAccess.getAuthData(auth2.authToken()));
    }
    @Test
    @Order(2)
    @DisplayName("Register Positive")
    public void registerPositive() {
        UserData user1 = new UserData("Jacob","jacobskarda@gmail.com","12345");
        try {
            RegisterResponse response = userService.register(user1);
            Assertions.assertNotNull(response, "Did not get response");
            Assertions.assertEquals(user1.username(), response.username(), "response username mismatch");
            Assertions.assertEquals(user1.username(), dataAccess.getUser(user1.username()).username(), "database username mismatch");
        } catch (BadRequestException e) {
            Assertions.fail("Returned bad request when it shouldn't have");
        }
        catch (AlreadyTakenException e) {
            Assertions.fail("Returned already taken when it shouldn't have");
        }
    }
    @Test
    @Order(3)
    @DisplayName("Register Negative")
    public void registerNegative() {
        UserData user1 = new UserData("Jacob",null,"12345");
        try {
            RegisterResponse response = userService.register(user1);
            Assertions.fail("Expected BadRequestException, got no exception");
        } catch (BadRequestException e) {
            return;
        }
        catch (AlreadyTakenException e) {
            Assertions.fail("Expected BadRequestException, got AlreadyTakenException");
        }
    }
}