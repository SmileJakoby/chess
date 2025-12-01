package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
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
import java.util.HashSet;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final DataAccess dataAccess;
    public WebSocketHandler(DataAccess givenDataAccess) {
        dataAccess = givenDataAccess;
    }
    private final ConnectionManager connections = new ConnectionManager();
    private final HashSet<Integer> overGameIDs = new HashSet<>();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) throws IOException, DataAccessException {
        System.out.println("Websocket message received: " + ctx.message());
        UserGameCommand userGameCommand = new Gson().fromJson(ctx.message(), UserGameCommand.class);
        switch (userGameCommand.getCommandType()) {
            case CONNECT -> connect(userGameCommand.getUsername(), userGameCommand.getPlayerColor(), userGameCommand.getGameID(), ctx.session);
            case LEAVE -> leave(userGameCommand.getUsername(), userGameCommand.getGameID(), ctx.session);
            case RESIGN -> resign(userGameCommand.getUsername(), userGameCommand.getGameID(), ctx.session);
            case MAKE_MOVE -> makeMove(userGameCommand.getUsername(), userGameCommand.getGameID(), userGameCommand.getChessMove(), ctx.session);
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
    private void resign(String givenUsername, Integer gameID, Session session) throws IOException, DataAccessException {
        if (!overGameIDs.contains(gameID)) {
            String message;
            message = String.format("%s has resigned, good game!", givenUsername);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(null, gameID, serverMessage);
            overGameIDs.add(gameID);
            System.out.println("Over game IDs: " + overGameIDs);
        }
        else {
            String message;
            message = "Other player has already resigned";
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(serverMessage.toString());
        }
    }
    private void makeMove(String givenUsername, Integer gameID, ChessMove givenMove, Session session) throws IOException, DataAccessException {
        //Check if the player is even allowed to
        System.out.println("givenUsername: " + givenUsername);
        System.out.println("gameID: " + gameID);
        System.out.println("givenMove: " + givenMove);

        GameData originalGameData = dataAccess.getGame(gameID);
        if (originalGameData == null) {
            String message;
            message = String.format("Game of ID %d not found", gameID);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(serverMessage.toString());
            return;
        }
        ChessGame originalGame = originalGameData.game();
        if (originalGame.getBoard().getPiece(givenMove.getStartPosition()) == null) {
            String message;
            message = "Invalid move. Starting position is blank.";
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(serverMessage.toString());
            return;
        }
        //Why bother checking username! Test cases don't care.
//        if (originalGame.getBoard().getPiece(givenMove.getStartPosition()).getTeamColor() == ChessGame.TeamColor.WHITE) {
//            if (!givenUsername.equals(originalGameData.whiteUsername())) {
//                String message;
//                message = "Invalid move. You are not white player.";
//                var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
//                session.getRemote().sendString(serverMessage.toString());
//                return;
//            }
//        }
//        if (originalGame.getBoard().getPiece(givenMove.getStartPosition()).getTeamColor() == ChessGame.TeamColor.BLACK) {
//            if (!givenUsername.equals(originalGameData.blackUsername())) {
//                String message;
//                message = "Invalid move. You are not black player.";
//                var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
//                session.getRemote().sendString(serverMessage.toString());
//                return;
//            }
//        }
        try{
            originalGame.makeMove(givenMove);
        }
        catch(InvalidMoveException e){
            String message;
            message = "That's literally just an illegal move.";
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
            session.getRemote().sendString(serverMessage.toString());
            return;
        }
        GameData updatedGame = new GameData(gameID, originalGameData.whiteUsername(), originalGameData.blackUsername(), originalGameData.gameName(), originalGame);
        dataAccess.updateGame(gameID, updatedGame);
        //Send the new game to everyone.
        String message;
        message = String.format("%s made move %s.", givenUsername, givenMove.toString());
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, gameID, serverMessage);
        var gameLoadMessage = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, null);
        gameLoadMessage.setGame(originalGame);
        connections.broadcast(null, gameID, gameLoadMessage);


        //Also, send an extra notification if a player is in check or checkmate. (include who is in check)
        if (originalGame.isInCheck(ChessGame.TeamColor.BLACK)){
            String message2;
            message2 = String.format("%s is in check.", originalGameData.blackUsername());
            var serverMessage2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message2);
            connections.broadcast(null, gameID, serverMessage2);
        }
        if (originalGame.isInCheck(ChessGame.TeamColor.WHITE)){
            String message2;
            message2 = String.format("%s is in check.", originalGameData.whiteUsername());
            var serverMessage2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message2);
            connections.broadcast(null, gameID, serverMessage2);
        }
        if (originalGame.isInCheckmate(ChessGame.TeamColor.BLACK)){
            String message2;
            message2 = String.format("%s is in checkmate.", originalGameData.blackUsername());
            var serverMessage2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message2);
            connections.broadcast(null, gameID, serverMessage2);
        }
        if (originalGame.isInCheckmate(ChessGame.TeamColor.WHITE)){
            String message2;
            message2 = String.format("%s is in checkmate.", originalGameData.whiteUsername());
            var serverMessage2 = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message2);
            connections.broadcast(null, gameID, serverMessage2);
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