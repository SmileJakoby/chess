package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.CreateGameResponse;
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

    public CreateGameResponse createGame(AuthData givenAuth, String gameName) throws UnauthorizedException, BadRequestException{
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }
        if (dataAccess.getGameByName(gameName) != null || gameName == null)
        {
            throw new BadRequestException("bad request");
        }
        else {
            Integer gameID = dataAccess.getGameCount() + 1;
            ChessGame newGame = new ChessGame();
            GameData newGameData = new GameData(gameID, null, null, gameName, newGame);
            dataAccess.addGame(newGameData);
            return new CreateGameResponse(gameID);
        }
    }

}
