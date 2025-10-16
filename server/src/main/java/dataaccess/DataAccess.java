package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

public interface DataAccess {
    void addUser(UserData user);
    UserData getUser(String username);
    void clear();
    void addAuthData(AuthData authData);
    AuthData getAuthData(String authToken);
    void removeAuthData(String authToken);
    GameData[] getGameDataList();
    void addGame(GameData gameData);
    GameData getGame(Integer gameID);
    void addPlayer(Integer gameID, String username, String playerColor) throws DataAccessException;
}
