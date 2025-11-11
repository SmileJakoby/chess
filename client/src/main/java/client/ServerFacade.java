package client;

import com.google.gson.Gson;
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
    public HttpResponse<String> Logout(UserData request){
        return null;
    }
    public HttpResponse<String> CreateGame(UserData request){
        return null;
    }
    public HttpResponse<String> ListGames(UserData request){
        return null;
    }
    public HttpResponse<String> JoinGame(UserData request){
        return null;
    }
    public HttpResponse<String> ObserveGame(UserData request){
        return null;
    }
}
