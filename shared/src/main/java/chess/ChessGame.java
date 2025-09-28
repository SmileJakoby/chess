package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor currentTeam;
    ChessBoard mainBoard;
    public ChessGame() {
        currentTeam = TeamColor.WHITE;
        mainBoard = new ChessBoard();
        mainBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        //System.out.println("Valid Moves called");
        HashSet<ChessMove> returnList = new HashSet<>();
        ChessPiece myPiece = mainBoard.getPiece(startPosition);
        HashSet<ChessMove> myPiecesMoves = (HashSet<ChessMove>) myPiece.pieceMoves(mainBoard, startPosition);
        for (ChessMove move : myPiecesMoves){
            if (mainBoard.moveIsValid(move,mainBoard.getPiece(move.getStartPosition()).getTeamColor())){
                returnList.add(move);
                //System.out.println("Added " + move);
            }
        }
        return returnList;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (mainBoard.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException("That's not even a piece, buddy.");
        }
        HashSet<ChessMove> returnList = new HashSet<>();
        ChessPiece myPiece = mainBoard.getPiece(move.getStartPosition());
        HashSet<ChessMove> myPiecesMoves = (HashSet<ChessMove>) myPiece.pieceMoves(mainBoard, move.getStartPosition());
        for (ChessMove move2 : myPiecesMoves){
            if (mainBoard.moveIsValid(move2,mainBoard.getPiece(move2.getStartPosition()).getTeamColor())){
                returnList.add(move2);
                //System.out.println("Added " + move);
            }
        }
        for (ChessMove move3: returnList){
            if (move3.equals(move))
            {
                mainBoard.MakeMove(move);
                if (currentTeam == TeamColor.WHITE){
                    currentTeam = TeamColor.BLACK;
                }
                else{
                    currentTeam = TeamColor.WHITE;
                }
                return;
            }
        }
        throw new InvalidMoveException("Not legal");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return mainBoard.CheckForCheck(teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return mainBoard.CheckForCheckMate(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return mainBoard.CheckForStalemate(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        mainBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return mainBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTeam == chessGame.currentTeam && Objects.equals(mainBoard, chessGame.mainBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeam, mainBoard);
    }
}
