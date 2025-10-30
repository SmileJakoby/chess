package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLDataAccess implements DataAccess {

    public SQLDataAccess() {
        try {configureDatabase();}
        catch(Exception e) {System.out.println(e.getMessage());}
    }

    @Override
    public void clear() throws DataAccessException {
        var userDeleteStatement = "DELETE FROM users";
        var authDeleteStatement = "DELETE FROM auth";
        var gameDeleteStatement = "DELETE FROM game";
        try{executeUpdate(userDeleteStatement);}
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){System.out.println(e.getMessage());}
        try{executeUpdate(authDeleteStatement);}
        catch(Exception e){System.out.println(e.getMessage());}
        try{executeUpdate(gameDeleteStatement);}
        catch(Exception e){System.out.println(e.getMessage());}
    }
    @Override
    public void addUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        String hash = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        try{executeUpdate(statement, user.username(), user.email(), hash);}
        catch(DataAccessException ex){
            throw new DataAccessException(ex.getMessage(), ex);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, email, password FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUserData(rs);
                    }
                }
            }
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }
    private UserData readUserData (ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var email = rs.getString("email");
        var password = rs.getString("password");
        return new UserData(username, email, password);
    }

    @Override
    public void addAuthData(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try{executeUpdate(statement, authData.authToken(), authData.username());}
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuthData(rs);
                    }
                }
            }
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    private AuthData readAuthData (ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        return new AuthData(authToken, username);
    }

    @Override
    public void removeAuthData(String authToken) throws DataAccessException {
        var userDeleteStatement = "DELETE FROM auth WHERE authToken = ?";
        try{executeUpdate(userDeleteStatement, authToken);}
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){System.out.println(e.getMessage());}
    }

    @Override
    public GameData[] getGameDataList() throws DataAccessException {
        GameData[] returnList = new GameData[getGameCount()];
        int i = 0;
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameid FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        returnList[i] = getGame(rs.getInt(1));
                        i++;
                    }
                }
            }
            return returnList;
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
            //return returnList;
        }
        //return returnList;
    }

    @Override
    public Integer getGameCount() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT COUNT(*) FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public int addGame(GameData gameData) throws DataAccessException {
        var statement = "INSERT INTO game (whiteusername, blackusername, gamename, game) VALUES (?, ?, ?, ?)";
        String json = new Gson().toJson(gameData.game());
        try{
            return executeUpdate(statement, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), json);
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){System.out.println(e.getMessage());}
        return 0;
    }

    @Override
    public GameData getGame(Integer gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameid, whiteusername, blackusername, gamename, game FROM game WHERE gameid = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGameData(rs);
                    }
                }
            }
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public GameData getGameByName(String gameName) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameid, whiteusername, blackusername, gamename, game FROM game WHERE gamename = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, gameName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGameData(rs);
                    }
                }
            }
        }
        catch (DataAccessException e){
            throw new DataAccessException(e.getMessage(), e);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    private GameData readGameData (ResultSet rs) throws SQLException {

        var gameID = rs.getInt("gameid");
        var whiteUsername = rs.getString("whiteusername");
        var blackUsername = rs.getString("blackusername");
        var gameName = rs.getString("gamename");
        var gameJson = rs.getString("game");
        ChessGame game = new Gson().fromJson(gameJson, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    @Override
    public void addPlayer(Integer gameID, String username, String playerColor) throws DataAccessException {
        if (playerColor.equals("BLACK"))
        {
            var statement = "UPDATE game SET blackusername = ? WHERE gameid = ?";
            try{executeUpdate(statement, username, gameID);}
            catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
            catch(Exception e){System.out.println(e.getMessage());}
        }
        if (playerColor.equals("WHITE"))
        {
            var statement = "UPDATE game SET whiteusername = ? WHERE gameid = ?";
            try{executeUpdate(statement, username, gameID);}
            catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
            catch(Exception e){System.out.println(e.getMessage());}
        }
    }
    @Override
    public void updateGame(Integer gameID, GameData game) throws DataAccessException {
        var statement = "UPDATE game SET game = ? WHERE gameid = ?";
        try{
            String json = new Gson().toJson(game.game());
            executeUpdate(statement, json, gameID);
        }
        catch(DataAccessException ex){throw new DataAccessException(ex.getMessage(), ex);}
        catch(Exception e){System.out.println(e.getMessage());}
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
                gameid INT NOT NULL AUTO_INCREMENT,
                whiteusername VARCHAR(255) DEFAULT NULL,
                blackusername VARCHAR(255) DEFAULT NULL,
                gamename VARCHAR(255) NOT NULL,
                game LONGTEXT NOT NULL,
                PRIMARY KEY (gameid)
                )"""
    };
    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
            //This was originally a try with resources statement. It made this too deeply nested, so... screw resource management!
            //try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    switch (param) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
//          }
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
