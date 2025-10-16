package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> usersMap = new HashMap<>();
    private final HashMap<String, AuthData> authMap = new HashMap<>();
    private final HashMap<Integer, GameData> gameMap = new HashMap<>();

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

    @Override
    public void removeAuthData(String authToken) {
        authMap.remove(authToken);
    }

    @Override
    public GameData[] getGameDataList(){
        GameData[] returnList = new GameData[gameMap.size()];
        for (int i = 0; i < gameMap.size(); i++) {
            returnList[i] = gameMap.get(i);
        }
        return returnList;
    }

    @Override
    public void addGame(GameData gameData){
        gameMap.put(gameData.gameID(), gameData);
    }

    @Override
    public GameData getGame(Integer gameID){
        return gameMap.get(gameID);
    }

    @Override
    public void addPlayer(Integer gameID, String username, String playerColor) throws DataAccessException{
        if (gameMap.get(gameID) != null) {
            if (playerColor.equals("black"))
            {
                GameData oldGameData = gameMap.get(gameID);
                GameData newGameData = new GameData(oldGameData.gameID(), oldGameData.whiteUsername(), username, oldGameData.gameName(), oldGameData.game());
                gameMap.remove(gameID);
                gameMap.put(gameID, newGameData);
            }
            if (playerColor.equals("white"))
            {
                GameData oldGameData = gameMap.get(gameID);
                GameData newGameData = new GameData(oldGameData.gameID(), username, oldGameData.blackUsername(), oldGameData.gameName(), oldGameData.game());
                gameMap.remove(gameID);
                gameMap.put(gameID, newGameData);
            }
        }
        else {
            throw new DataAccessException("bad request");
        }
    }
}
