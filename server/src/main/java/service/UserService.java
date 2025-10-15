package service;

import dataaccess.DataAccess;
import datamodel.RegisterResponse;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;
    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }
    public RegisterResponse register(UserData user) throws Exception, AlreadyTakenException {
        var existingUser = dataAccess.getUser(user.username());
        if (existingUser != null) {
            //TODO: Make this a ServiceException
            throw new AlreadyTakenException("Error: Already exists");
        }
        if (user.password() == null) {
            throw new Exception("Error: Password is null");
        }
        dataAccess.addUser(user);
        String authToken = GenerateAuthToken();
        AuthData newAuthData = new AuthData(authToken, user.username());
        dataAccess.addAuthData(newAuthData);
        return new RegisterResponse(user.username(), authToken);
    }

    private String GenerateAuthToken()
    {
        return UUID.randomUUID().toString();
    }
}
