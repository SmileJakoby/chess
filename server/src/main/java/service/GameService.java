package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.DataAccess;

import dataaccess.DataAccessException;
import datamodel.CreateGameResponse;
import datamodel.GameResponse;
import datamodel.GamesListResponse;

import model.AuthData;
import model.GameData;

import java.util.Objects;


public class GameService {
    private final DataAccess dataAccess;
    public GameService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public GamesListResponse getGamesList(AuthData givenAuth) throws UnauthorizedException, DataAccessException {
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }

        GameData[] allGames = dataAccess.getGameDataList();
        GameResponse[] returnList = new GameResponse[allGames.length];
        for (int i = 0; i < allGames.length; i++) {
            var item = allGames[i];
            GameResponse insertResponse = new GameResponse(item.gameID(), item.whiteUsername(), item.blackUsername(), item.gameName());
            returnList[i] = insertResponse;
        }
        return new GamesListResponse(returnList);
    }

    public CreateGameResponse createGame(AuthData givenAuth, String gameName) throws UnauthorizedException, BadRequestException, DataAccessException {
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }
        try {
            if (dataAccess.getGameByName(gameName) != null || gameName == null) {
                throw new BadRequestException("bad request");
            } else {
                Integer gameID = dataAccess.getGameCount() + 1;
                ChessGame newGame = new ChessGame();
                GameData newGameData = new GameData(gameID, null, null, gameName, newGame);
                int returnInt = dataAccess.addGame(newGameData);
                return new CreateGameResponse(returnInt);
            }
        }
        catch(DataAccessException e){
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void joinGame(AuthData givenAuth, String playerColor, Integer gameID)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }
        if (dataAccess.getGame(gameID) == null || playerColor == null || gameID == null)
        {
            throw new BadRequestException("bad request");
        }
        if (!(playerColor.equals("WHITE") || playerColor.equals("BLACK")))
        {
            throw new  BadRequestException("bad request");
        }
        if (playerColor.equals("WHITE"))
        {
            if (dataAccess.getGame(gameID).whiteUsername() != null)
            {
                throw new AlreadyTakenException("already taken");
            }
        }
        if (playerColor.equals("BLACK"))
        {
            if (dataAccess.getGame(gameID).blackUsername() != null)
            {
                throw new AlreadyTakenException("already taken");
            }
        }
        dataAccess.addPlayer(gameID, dataAccess.getAuthData(givenAuth.authToken()).username(), playerColor);
    }

    public void makeMove(AuthData givenAuth, ChessMove givenMove, Integer gameID) throws DataAccessException, UnauthorizedException, BadRequestException {
        String authUsername;
        if (dataAccess.getAuthData(givenAuth.authToken()) == null)
        {
            throw new UnauthorizedException("unauthorized");
        }
        else {
            authUsername = dataAccess.getAuthData(givenAuth.authToken()).username();
        }
        if (dataAccess.getGame(gameID) == null || gameID == null)
        {
            throw new BadRequestException("Can't find that game");
        }
        GameData foundGameData = dataAccess.getGame(gameID);
        ChessGame.TeamColor colorOfPiece = foundGameData.game().getBoard().getPiece(givenMove.getStartPosition()).getTeamColor();
        boolean isProperPlayer = false;
        if (Objects.equals(authUsername, foundGameData.whiteUsername()))
        {
            if (colorOfPiece == ChessGame.TeamColor.WHITE)
            {
                isProperPlayer = true;
            }
        }
        if (Objects.equals(authUsername, foundGameData.blackUsername()))
        {
            if (colorOfPiece == ChessGame.TeamColor.BLACK)
            {
                isProperPlayer = true;
            }
        }
        if (!isProperPlayer)
        {
            throw new BadRequestException("That is not your piece");
        }
        try{
            foundGameData.game().makeMove(givenMove);
            dataAccess.updateGame(gameID, foundGameData);
        }
        catch(InvalidMoveException e){
            throw new BadRequestException("Invalid Move");
        }
    }
}
