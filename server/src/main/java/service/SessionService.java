package service;

import dataaccess.DataAccess;
import datamodel.LoginResponse;
import datamodel.RegisterResponse;
import model.AuthData;
import model.UserData;

import java.util.Objects;
import java.util.UUID;

public class SessionService {
    private final DataAccess dataAccess;
    public SessionService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public LoginResponse login(UserData user) throws BadRequestException, UnauthorizedException {
        var existingUser = dataAccess.getUser(user.username());

        if (dataAccess.getUser(user.username()) == null) {
            throw new BadRequestException("bad request");
        }
        if (!Objects.equals(dataAccess.getUser(user.username()).password(), user.password())) {
            throw new UnauthorizedException("unauthorized");
        }
        String authToken = GenerateAuthToken();
        AuthData newAuthData = new AuthData(authToken, user.username());
        dataAccess.addAuthData(newAuthData);
        return new LoginResponse(user.username(), authToken);
    }
    private String GenerateAuthToken()
    {
        return UUID.randomUUID().toString();
    }
}
