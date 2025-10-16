package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import model.AuthData;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

public class Server {

    private final Javalin server;
    private final UserService userService;
    private final SessionService sessionService;
    private final GameService gameService;
    private final DatabaseService databaseService;
    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        sessionService = new SessionService(dataAccess);
        gameService = new GameService(dataAccess);
        databaseService = new DatabaseService(dataAccess);
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
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    private void clear(Context ctx){
        databaseService.clear();
        ctx.result("{}");
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
        catch(UnauthorizedException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(401).result(errorMsg);
        }
        catch(BadRequestException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(400).result(errorMsg);
        }
    }

}
