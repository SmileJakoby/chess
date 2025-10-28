package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLDataAccess implements DataAccess {
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

    public void example() throws Exception {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT 1+1")) {
                var rs = preparedStatement.executeQuery();
                rs.next();
                System.out.println(rs.getInt(1));
            }
        }
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
    @Override
    public void updateGame(Integer gameID, GameData game){
        gameMap.remove(gameID);
        gameMap.put(gameID, game);
    }

    private final String[] createStatements = {
            """
                CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                PRIMARY KEY (username)
                )""",
            """
                CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
                )""",
            """
                CREATE TABLE IF NOT EXISTS game (
                gameid VARCHAR(255) NOT NULL AUTO_INCREMENT,
                whiteusername VARCHAR(255) DEFAULT NULL,
                blackusername VARCHAR(255) DEFAULT NULL,
                gamename VARCHAR(255) NOT NULL,
                game MEDIUMBLOB NOT NULL,
                PRIMARY KEY (gameid)
                )"""
    };
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof ChessGame p){
                        var json = new Gson().toJson(p);
                        ps.setString(i + 1, json);
                    }
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()){
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        }
        catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }
}
