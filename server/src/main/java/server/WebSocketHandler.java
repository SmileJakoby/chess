package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import service.DatabaseService;
import service.GameService;
import websocket.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final DataAccess dataAccess;
    public WebSocketHandler(DataAccess givenDataAccess) {
        dataAccess = givenDataAccess;
    }
    private final ConnectionManager connections = new ConnectionManager();


    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) throws IOException, DataAccessException {
        System.out.println("Websocket message received: ");
        UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(userGameCommand.getUsername(), userGameCommand.getPlayerColor(), userGameCommand.getGameID(), ctx.session);
            case LEAVE -> leave(userGameCommand.getUsername(), userGameCommand.getGameID(), ctx.session);
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(String givenUsername, String givenPlayerColor, Integer gameID, Session session) throws IOException, DataAccessException {
        connections.add(gameID, session);
        String message;
        if (givenPlayerColor != null) {
            message = String.format("%s joined the game as %s.", givenUsername, givenPlayerColor);
        }
        else {
            message = String.format("%s is observing the game.", givenUsername);
        }
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, gameID, serverMessage);
        var gameLoadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null);
        ChessGame foundGame = dataAccess.getGame(gameID).game();
        gameLoadMessage.setGame(foundGame);
        session.getRemote().sendString(gameLoadMessage.toString());
    }
//
    private void leave(String givenUsername, Integer gameID, Session session) throws IOException, DataAccessException {
        var message = String.format("%s left the game", givenUsername);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, gameID, serverMessage);
        connections.remove(gameID, session);
        GameData gameData = dataAccess.getGame(gameID);
        String safeWhiteName = gameData.whiteUsername();
        if (safeWhiteName == null)
        {
            safeWhiteName = "";
        }
        String safeBlackName = gameData.blackUsername();
        if (safeBlackName == null)
        {
            safeBlackName = "";
        }
        if (safeWhiteName.equals(givenUsername) && safeBlackName.equals(givenUsername)) {
            dataAccess.addPlayer(gameID, null, "WHITE");
            dataAccess.addPlayer(gameID, null, "BLACK");
        }
        else {
            if (safeWhiteName.equals(givenUsername)) {
                dataAccess.addPlayer(gameID, null, "WHITE");
            }
            if (safeBlackName.equals(givenUsername)) {
                dataAccess.addPlayer(gameID, null, "BLACK");
            }
        }
    }
//
//    public void makeNoise(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast(null, notification);
//        } catch (Exception ex) {
//            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
//        }
//    }
}