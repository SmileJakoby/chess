package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.SQLDataAccess;
import datamodel.JoinGameRequest;
import jakarta.websocket.Session;
import model.GameData;
import model.UserData;
import model.AuthData;
import io.javalin.*;
import io.javalin.http.Context;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import service.*;
import websocket.commands.UserGameCommand;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final SessionService sessionService;
    private final GameService gameService;
    private final DatabaseService databaseService;
    private final WebSocketHandler webSocketHandler;
    public Server() {
        var dataAccess = new SQLDataAccess();
        userService = new UserService(dataAccess);
        sessionService = new SessionService(dataAccess);
        gameService = new GameService(dataAccess);
        databaseService = new DatabaseService(dataAccess);
        webSocketHandler = new WebSocketHandler(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));


        server.delete("db", ctx -> clear(ctx));
        //Needs to return username+authToken
        //server.post("user", ctx->ctx.result("{\"username\":\" \", \"authToken\":\" \"}"));
        server.post("user", ctx->register(ctx));
        // Register your endpoints and exception handlers here.
        server.post("session", ctx->login(ctx));
        server.delete("session", ctx->logout(ctx));
        server.get("game", ctx->listGames(ctx));
        server.post("game",ctx->createGame(ctx));
        server.put("game",ctx->joinGame(ctx));


        server.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler);
            ws.onMessage(webSocketHandler);
            ws.onClose(webSocketHandler);
        });

    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    private void clear(Context ctx) {
        try {
            databaseService.clear();
            ctx.result("{}");
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
    }
    private void register(Context ctx){
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var registrationResponse = userService.register(user);

            ctx.result(serializer.toJson(registrationResponse));
        }
        catch(BadRequestException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(400).result(errorMsg);
        }
        catch(AlreadyTakenException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(403).result(errorMsg);
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
    }
    private void login(Context ctx){
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var loginResponse = sessionService.login(user);

            ctx.result(serializer.toJson(loginResponse));
        }
        catch(BadRequestException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(400).result(errorMsg);
        }
        catch(UnauthorizedException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(401).result(errorMsg);
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
    }
    private void logout(Context ctx){
        try {
            String reqJson = ctx.header("authorization");
            var givenAuth = new AuthData(reqJson, null);
            sessionService.logout(givenAuth);

            ctx.result("{}");
        }
        catch(UnauthorizedException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(401).result(errorMsg);
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
    }
    private void listGames(Context ctx){
        try {
            var serializer = new Gson();
            String reqJson = ctx.header("authorization");
            var givenAuth = new AuthData(reqJson, null);
            var gamesListResponse = gameService.getGamesList(givenAuth);

            ctx.result(serializer.toJson(gamesListResponse));
        }
        catch(UnauthorizedException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(401).result(errorMsg);
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
    }

    private void createGame(Context ctx){
        try {
            var serializer = new Gson();
            String reqJson = ctx.header("authorization");
            var givenAuth = new AuthData(reqJson, null);

            String reqJsonBody = ctx.body();
            var game = serializer.fromJson(reqJsonBody, GameData.class);

            var createGameResponse = gameService.createGame(givenAuth, game.gameName());

            ctx.result(serializer.toJson(createGameResponse));
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
        catch(UnauthorizedException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(401).result(errorMsg);
        }
        catch(BadRequestException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(400).result(errorMsg);
        }

    }

    private void joinGame(Context ctx){
        try {
            var serializer = new Gson();
            String reqJson = ctx.header("authorization");
            var givenAuth = new AuthData(reqJson, null);

            String reqJsonBody = ctx.body();
            var joinRequest = serializer.fromJson(reqJsonBody, JoinGameRequest.class);

            gameService.joinGame(givenAuth, joinRequest.playerColor(), joinRequest.gameID());
            ctx.result("{}");
        }
        catch(UnauthorizedException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(401).result(errorMsg);
        }
        catch(BadRequestException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(400).result(errorMsg);
        }
        catch(AlreadyTakenException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(403).result(errorMsg);
        }
        catch(DataAccessException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(500).result(errorMsg);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg){
        //deserialize WebSocket message from client
        var serializer = new Gson();
        UserGameCommand command = serializer.fromJson(msg, UserGameCommand.class);

        // handle WebSocket message from client
        System.out.println("Received from " + session.toString() + ": " + msg);

    }
}
