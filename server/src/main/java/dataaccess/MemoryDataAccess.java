package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> usersMap = new HashMap<>();
    private final HashMap<String, AuthData> authMap = new HashMap<>();
    @Override
    public void clear(){
        usersMap.clear();
        authMap.clear();
    }
    @Override
    public void addUser(UserData user) {
        usersMap.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return usersMap.get(username);
    }


    @Override
    public void addAuthData(AuthData authData) {
        authMap.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return authMap.get(authToken);
    }
}
