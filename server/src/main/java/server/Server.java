package server;

import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.http.Context;

import java.util.Map;

public class Server {

    private final Javalin server;

    public Server() {
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
        var serializer = new Gson();
        var requestBody = serializer.fromJson(ctx.body(), Map.class);
        //TODO: Call service to register the user
        var response = Map.of("username", requestBody.get("username"), "authToken", "xyz");
        ctx.result(serializer.toJson(response));
    }
}
