package service;

import dataaccess.DataAccess;
import datamodel.LoginResponse;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class SessionService {
    private final DataAccess dataAccess;
    public SessionService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public LoginResponse login(UserData user) throws BadRequestException, UnauthorizedException {

        if (user.password() == null || user.username() == null) {
            throw new BadRequestException("bad request");
        }
        if (dataAccess.getUser(user.username()) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        else
        {
            if (!dataAccess.getUser(user.username()).password().equals(user.password())) {
                throw new UnauthorizedException("unauthorized");
            }
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
