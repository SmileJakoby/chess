package service;

import dataaccess.DataAccess;
import datamodel.GameResponse;
import datamodel.GamesListResponse;
import datamodel.RegisterResponse;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.UUID;

public class GameService {
    private final DataAccess dataAccess;
    public GameService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public GamesListResponse getGamesList(AuthData givenAuth) throws UnauthorizedException{
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }
        GameData[] allGames = dataAccess.getGameDataList();
        GameResponse[] returnList = new GameResponse[allGames.length];
        for (int i = 0; i < allGames.length; i++) {
            returnList[i] = new GameResponse(allGames[i].gameID(), allGames[i].whiteUsername(), allGames[i].blackUsername(), allGames[i].gameName());
        }
        return new GamesListResponse(returnList);
    }
}
