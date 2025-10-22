package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

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


    private void generateMoves(ChessBoard board, ChessPosition startingPos, ChessPosition currentPos,
                               HashSet<ChessMove> givenSet, int rowAdd, int colAdd, boolean recursive){
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
                generateMoves(board, myPosition, myPosition, returnList, -1, -1, true);
                generateMoves(board, myPosition, myPosition, returnList, -1, 1, true);
                generateMoves(board, myPosition, myPosition, returnList, 1, 1, true);
                generateMoves(board, myPosition, myPosition, returnList, 1, -1, true);
                return returnList;
            case KING:
                generateMoves(board, myPosition, myPosition, returnList, 1, 0, false);
                generateMoves(board, myPosition, myPosition, returnList, 1, 1, false);
                generateMoves(board, myPosition, myPosition, returnList, 0, 1, false);
                generateMoves(board, myPosition, myPosition, returnList, -1, 1, false);
                generateMoves(board, myPosition, myPosition, returnList, -1, 0, false);
                generateMoves(board, myPosition, myPosition, returnList, -1, -1, false);
                generateMoves(board, myPosition, myPosition, returnList, 0, -1, false);
                generateMoves(board, myPosition, myPosition, returnList, 1, -1, false);
                return returnList;
            case KNIGHT:
                generateMoves(board, myPosition, myPosition, returnList, 2, 1, false);
                generateMoves(board, myPosition, myPosition, returnList, 1, 2, false);
                generateMoves(board, myPosition, myPosition, returnList, -1, 2, false);
                generateMoves(board, myPosition, myPosition, returnList, -2, 1, false);
                generateMoves(board, myPosition, myPosition, returnList, -2, -1, false);
                generateMoves(board, myPosition, myPosition, returnList, -1, -2, false);
                generateMoves(board, myPosition, myPosition, returnList, 1, -2, false);
                generateMoves(board, myPosition, myPosition, returnList, 2, -1, false);
                return returnList;
            case PAWN:
                int rowMoveDirection;
                if (this.pieceColor == ChessGame.TeamColor.WHITE){
                    rowMoveDirection = 1;
                }
                else {
                    rowMoveDirection = -1;
                }
                //Check for forward move
                int nextRow = myPosition.getRow() + rowMoveDirection;
                int nextCol = myPosition.getColumn();
                ChessPosition nextPosition;
                ChessPiece pieceInWay;
                if (nextRow >= 1 && nextRow <= 8 && nextCol >= 1 && nextCol <= 8) {
                    nextPosition = new ChessPosition(nextRow, nextCol);
                    pieceInWay = board.getPiece(nextPosition);
                    if (pieceInWay == null && this.pieceColor == ChessGame.TeamColor.WHITE) {
                        pawnMove(myPosition, returnList, nextPosition, 8);
                        if (myPosition.getRow() == 2) {
                            calculatePawnExtraMove(board, myPosition, returnList, rowMoveDirection);
                        }
                    }
                    if (pieceInWay == null && this.pieceColor == ChessGame.TeamColor.BLACK) {
                        pawnMove(myPosition, returnList, nextPosition, 1);
                        if (myPosition.getRow() == 7) {
                            calculatePawnExtraMove(board, myPosition, returnList, rowMoveDirection);
                        }
                    }
                }
                nextRow = myPosition.getRow() + rowMoveDirection;
                nextCol = myPosition.getColumn() - 1;
                pawnDiagonalCheck(board, myPosition, returnList, nextRow, nextCol);
                nextRow = myPosition.getRow() + rowMoveDirection;
                nextCol = myPosition.getColumn() + 1;
                pawnDiagonalCheck(board, myPosition, returnList, nextRow, nextCol);
                return returnList;
            case QUEEN:
                generateMoves(board, myPosition, myPosition, returnList, 1, 0, true);
                generateMoves(board, myPosition, myPosition, returnList, 1, 1, true);
                generateMoves(board, myPosition, myPosition, returnList, 0, 1, true);
                generateMoves(board, myPosition, myPosition, returnList, -1, 1, true);
                generateMoves(board, myPosition, myPosition, returnList, -1, 0, true);
                generateMoves(board, myPosition, myPosition, returnList, -1, -1, true);
                generateMoves(board, myPosition, myPosition, returnList, 0, -1, true);
                generateMoves(board, myPosition, myPosition, returnList, 1, -1, true);
                return returnList;
            case ROOK:
                generateMoves(board, myPosition, myPosition, returnList, 1, 0, true);
                generateMoves(board, myPosition, myPosition, returnList, 0, 1, true);
                generateMoves(board, myPosition, myPosition, returnList, -1, 0, true);
                generateMoves(board, myPosition, myPosition, returnList, 0, -1, true);
                return returnList;
        }
        return List.of();
    }

    private void calculatePawnExtraMove(ChessBoard board, ChessPosition myPosition, HashSet<ChessMove> returnList, int rowMoveDirection) {
        ChessPosition nextPosition;
        ChessPiece pieceInWay;
        nextPosition = new ChessPosition(myPosition.getRow() + rowMoveDirection + rowMoveDirection, myPosition.getColumn());
        pieceInWay = board.getPiece(nextPosition);
        if (pieceInWay == null) {
            returnList.add(new ChessMove(myPosition, nextPosition, null));
        }
    }

    private void pawnMove(ChessPosition myPosition, HashSet<ChessMove> returnList, ChessPosition nextPosition, int promotionRow) {
        if (nextPosition.getRow() != promotionRow) {
            returnList.add(new ChessMove(myPosition, nextPosition, null));
        }
        else {
            returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
            returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
            returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
            returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
        }
    }

    private void pawnDiagonalCheck(ChessBoard board, ChessPosition myPosition, HashSet<ChessMove> returnList, int nextRow, int nextCol) {
        ChessPosition nextPosition;
        ChessPiece pieceInWay;
        if (nextRow >= 1 && nextRow <= 8 && nextCol >= 1 && nextCol <= 8) {
            nextPosition = new ChessPosition(nextRow, nextCol);
            pieceInWay = board.getPiece(nextPosition);
            if (pieceInWay != null) {
                if (pieceInWay.getTeamColor() != this.pieceColor) {
                    if (this.pieceColor == ChessGame.TeamColor.WHITE) {
                        pawnMove(myPosition, returnList, nextPosition, 8);
                    }
                    if (this.pieceColor == ChessGame.TeamColor.BLACK) {
                        pawnMove(myPosition, returnList, nextPosition, 1);
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
