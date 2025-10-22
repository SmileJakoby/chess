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
        gameMap.clear();
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
        int i = 0;
        for (HashMap.Entry<Integer, GameData> entry : gameMap.entrySet()){
            returnList[i] = entry.getValue();
            i++;
        }
        return returnList;
    }

    @Override
    public Integer getGameCount() {
        return gameMap.size();
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
    public GameData getGameByName(String gameName) {

        for (HashMap.Entry<Integer, GameData> entry : gameMap.entrySet()){
            if (entry.getValue().gameName().equals(gameName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void addPlayer(Integer gameID, String username, String playerColor){
        if (playerColor.equals("BLACK"))
        {
            GameData oldData = gameMap.get(gameID);
            GameData newGameData = new GameData(oldData.gameID(), oldData.whiteUsername(), username, oldData.gameName(), oldData.game());
            gameMap.remove(gameID);
            gameMap.put(gameID, newGameData);
        }
        if (playerColor.equals("WHITE"))
        {
            GameData oldData = gameMap.get(gameID);
            GameData newGameData = new GameData(oldData.gameID(), username, oldData.blackUsername(), oldData.gameName(), oldData.game());
            gameMap.remove(gameID);
            gameMap.put(gameID, newGameData);
        }
    }
}
