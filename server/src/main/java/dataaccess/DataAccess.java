package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.BadRequestException;

public interface DataAccess {
    void addUser(UserData user);
    UserData getUser(String username);
    void clear() throws DataAccessException;
    void addAuthData(AuthData authData);
    AuthData getAuthData(String authToken);
    void removeAuthData(String authToken);
    GameData[] getGameDataList();
    int addGame(GameData gameData);
    GameData getGame(Integer gameID);
    void addPlayer(Integer gameID, String username, String playerColor);
    GameData getGameByName(String gameName) throws DataAccessException;
    Integer getGameCount();
    void updateGame(Integer gameID, GameData game);
}
