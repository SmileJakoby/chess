package server;

import com.google.gson.Gson;
import dataaccess.MemoryDataAccess;
import model.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.UserService;

public class Server {

    private final Javalin server;
    private final UserService userService;

    public Server() {
        var dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));


        server.delete("db", ctx -> ctx.result("{}"));
        //Needs to return username+authToken
        //server.post("user", ctx->ctx.result("{\"username\":\" \", \"authToken\":\" \"}"));
        server.post("user", ctx->register(ctx));
        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }

    private void register(Context ctx){
        try {
            var serializer = new Gson();
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);

            var registrationResponse = userService.register(user);

            ctx.result(serializer.toJson(registrationResponse));
        }
        catch(AlreadyTakenException ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(403).result(errorMsg);
        }
        catch(Exception ex){
            var errorMsg = String.format("{ \"message\": \"Error: %s\" }", ex.getMessage());
            ctx.status(403).result(errorMsg);
        }
    }
}
