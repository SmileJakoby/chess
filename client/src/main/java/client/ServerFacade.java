package client;

import com.google.gson.Gson;
import datamodel.JoinGameRequest;
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
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public void setServerURL(String serverURL) {
        serverUrl = serverURL;
    }
    public HttpResponse<String> Register(UserData givenUserData) throws URISyntaxException, IOException, InterruptedException {
        var jsonBody = new Gson().toJson(givenUserData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/user"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> Login(UserData givenUserData) throws URISyntaxException, IOException, InterruptedException {
        var jsonBody = new Gson().toJson(givenUserData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/session"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> Logout(String givenAuthToken) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/session"))
                .DELETE()
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> CreateGame(GameData givenGame, String givenAuthToken) throws IOException, InterruptedException, URISyntaxException {
        var jsonBody = new Gson().toJson(givenGame);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/game"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> ListGames(String givenAuthToken) throws IOException, InterruptedException, URISyntaxException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/game"))
                .GET()
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> JoinGame(JoinGameRequest givenJoinRequest, String givenAuthToken) throws IOException, InterruptedException, URISyntaxException{
        var jsonBody = new Gson().toJson(givenJoinRequest);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/game"))
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .header("Content-Type", "application/json")
                .header("authorization", givenAuthToken)
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    public HttpResponse<String> ObserveGame(UserData request){
        //To be implemented in phase 6.
        return null;
    }
    public HttpResponse<String> clearDatabase() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(serverUrl + "/db"))
                .DELETE()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
