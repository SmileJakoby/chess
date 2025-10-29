package dataaccess;


import model.AuthData;
import model.GameData;
import model.UserData;

public interface DataAccess {
    void addUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
    void addAuthData(AuthData authData);
    AuthData getAuthData(String authToken) throws DataAccessException;
    void removeAuthData(String authToken) throws DataAccessException;
    GameData[] getGameDataList() throws DataAccessException;
    int addGame(GameData gameData) throws DataAccessException;
    GameData getGame(Integer gameID) throws DataAccessException;
    void addPlayer(Integer gameID, String username, String playerColor) throws DataAccessException;
    GameData getGameByName(String gameName) throws DataAccessException;
    Integer getGameCount() throws DataAccessException;
    void updateGame(Integer gameID, GameData game) throws DataAccessException;
}
