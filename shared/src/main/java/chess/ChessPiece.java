package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }


    private void generateMoves(ChessBoard board, ChessPosition startingPos, ChessPosition currentPos, HashSet<ChessMove> givenSet, int rowAdd, int colAdd, boolean recursive){
        int row = currentPos.getRow() + rowAdd;
        int col = currentPos.getColumn() + colAdd;
        if (row <= 8 && row >= 1 && col <= 8 && col >= 1) {
            ChessPosition nextPos = new ChessPosition(row, col);
            ChessPiece pieceInWay = board.getPiece(nextPos);
            if (pieceInWay == null) {
                givenSet.add(new ChessMove(startingPos, nextPos, null));
                if (recursive) {
                    generateMoves(board, startingPos, nextPos, givenSet, rowAdd, colAdd, recursive);
                }
            }
            else {
                if (pieceInWay.getTeamColor() == this.pieceColor) {
                    return;
                }
                if (pieceInWay.getTeamColor() != this.pieceColor) {
                    givenSet.add(new ChessMove(startingPos, nextPos, null));
                }
            }
        }
        return;
    }
    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        HashSet<ChessMove> returnList = new HashSet<>();
        switch(piece.getPieceType()) {
            case BISHOP:
                //Down and left
                generateMoves(board, myPosition, myPosition, returnList, -1, -1, true);
                //Down and right
                generateMoves(board, myPosition, myPosition, returnList, -1, 1, true);
                //Up and right
                generateMoves(board, myPosition, myPosition, returnList, 1, 1, true);
                //Up and left
                generateMoves(board, myPosition, myPosition, returnList, 1, -1, true);
                return returnList;
            case KNIGHT:
                return List.of();
            case QUEEN:
                return List.of();
        }
        return List.of();
    }
}
