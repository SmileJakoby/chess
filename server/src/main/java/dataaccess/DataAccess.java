package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    void addUser(UserData user);
    UserData getUser(String username);
    void clear();
    void addAuthData(AuthData authData);
    AuthData getAuthData(String authToken);
}
