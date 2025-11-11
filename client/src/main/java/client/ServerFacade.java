package client;

import datamodel.LoginResponse;
import datamodel.RegisterResponse;
import model.UserData;

import java.net.http.HttpResponse;

public class ServerFacade {
    public HttpResponse<String> Register(UserData request){
        return null;
    }
    public HttpResponse<String> Login(UserData request){
        return null;
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
