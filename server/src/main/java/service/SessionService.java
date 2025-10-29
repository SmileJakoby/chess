package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.LoginResponse;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class SessionService {
    private final DataAccess dataAccess;
    public SessionService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public LoginResponse login(UserData user) throws BadRequestException, UnauthorizedException, DataAccessException {

        if (user.password() == null || user.username() == null) {
            throw new BadRequestException("bad request");
        }
        if (dataAccess.getUser(user.username()) == null) {
            throw new UnauthorizedException("unauthorized");
        }
        else
        {
            if (!dataAccess.getUser(user.username()).password().equals(user.password())) {
                //String hash = BCrypt.hashpw(user.password(), BCrypt.gensalt());
                try{
                    if (!BCrypt.checkpw(user.password(), dataAccess.getUser(user.username()).password())) {
                        throw new UnauthorizedException("unauthorized");
                    }
                }
                catch(IllegalArgumentException e){
                    throw new UnauthorizedException("unauthorized");
                }
            }
        }
        String authToken = generateAuthToken();
        AuthData newAuthData = new AuthData(authToken, user.username());
        dataAccess.addAuthData(newAuthData);
        return new LoginResponse(user.username(), authToken);
    }
    public void logout(AuthData givenAuth) throws UnauthorizedException, DataAccessException  {
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }
        else {
            dataAccess.removeAuthData(givenAuth.authToken());
        }
    }
    private String generateAuthToken()
    {
        return UUID.randomUUID().toString();
    }
}
