package client;

import com.google.gson.Gson;
import ClientSideDataModel.JoinGameRequest;
import model.GameData;
import model.UserData;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ServerFacade {
    public static String serverUrl = "";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public void setServerURL(String serverURL) {
        serverUrl = serverURL;
    }

    public HttpResponse<String> register(UserData givenUserData) throws URISyntaxException, IOException, InterruptedException {
        var jsonBody = new Gson().toJson(givenUserData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> login(UserData givenUserData) throws URISyntaxException, IOException, InterruptedException {
        var jsonBody = new Gson().toJson(givenUserData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/session"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> logout(String givenAuthToken) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/session"))
                .DELETE()
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> createGame(GameData givenGame, String givenAuthToken) throws IOException, InterruptedException, URISyntaxException {
        var jsonBody = new Gson().toJson(givenGame);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/game"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> listGames(String givenAuthToken) throws IOException, InterruptedException, URISyntaxException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/game"))
                .GET()
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> joinGame(JoinGameRequest givenJoinGameRequest, String givenAuthToken)
            throws IOException, InterruptedException, URISyntaxException{
        var jsonBody = new Gson().toJson(givenJoinGameRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/game"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
    //Observe game needs to be implemented in phase 6

    public HttpResponse<String> clearDatabase() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/db"))
                .DELETE()
                .build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
