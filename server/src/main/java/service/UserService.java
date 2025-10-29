package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.RegisterResponse;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;
    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }
    public RegisterResponse register(UserData user) throws BadRequestException, AlreadyTakenException, DataAccessException {
        var existingUser = dataAccess.getUser(user.username());
        if (existingUser != null) {
            throw new AlreadyTakenException("already taken");
        }
        if (user.password() == null || user.username() == null || user.email() == null) {
            throw new BadRequestException("bad request");
        }
        dataAccess.addUser(user);
        String authToken = generateAuthToken();
        AuthData newAuthData = new AuthData(authToken, user.username());
        dataAccess.addAuthData(newAuthData);
        return new RegisterResponse(user.username(), authToken);
    }

    private String generateAuthToken()
    {
        return UUID.randomUUID().toString();
    }
}
