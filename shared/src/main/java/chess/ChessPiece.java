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
            case KING:
                //The generateMoves function was designed for recursive pieces. The knight and King suffer for this.
                //Up
                generateMoves(board, myPosition, myPosition, returnList, 1, 0, false);
                //UpRight
                generateMoves(board, myPosition, myPosition, returnList, 1, 1, false);
                //Right
                generateMoves(board, myPosition, myPosition, returnList, 0, 1, false);
                //RightDown
                generateMoves(board, myPosition, myPosition, returnList, -1, 1, false);
                //Down
                generateMoves(board, myPosition, myPosition, returnList, -1, 0, false);
                //DownLeft
                generateMoves(board, myPosition, myPosition, returnList, -1, -1, false);
                //Left
                generateMoves(board, myPosition, myPosition, returnList, 0, -1, false);
                //UpLeft
                generateMoves(board, myPosition, myPosition, returnList, 1, -1, false);
                return returnList;
            case KNIGHT:
                //The generateMoves function was designed for recursive pieces. The knight and King suffer for this.
                //Also, I'm not going to add comments for horse moves. They are all an L.
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
                //Pawn is the most complex piece, and basically requires entirely custom code.

                int RowMoveDirection;
                if (this.pieceColor == ChessGame.TeamColor.WHITE){
                    RowMoveDirection = 1;
                }
                else {
                    RowMoveDirection = -1;
                }

                //Check for forward move
                int nextRow = myPosition.getRow() + RowMoveDirection;
                int nextCol = myPosition.getColumn();
                ChessPosition nextPosition;
                ChessPiece pieceInWay;
                if (nextRow >= 1 && nextRow <= 8 && nextCol >= 1 && nextCol <= 8) {
                    nextPosition = new ChessPosition(nextRow, nextCol);
                    pieceInWay = board.getPiece(nextPosition);
                    if (pieceInWay == null) {
                        if (this.pieceColor == ChessGame.TeamColor.WHITE) {
                            if (nextPosition.getRow() != 8) {
                                returnList.add(new ChessMove(myPosition, nextPosition, null));
                            }
                            else {
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
                            }
                            //If white, and on second row (haven't moved yet) check for the next spot
                            if (myPosition.getRow() == 2) {
                                nextPosition = new ChessPosition(myPosition.getRow() + RowMoveDirection + RowMoveDirection, myPosition.getColumn());
                                pieceInWay = board.getPiece(nextPosition);
                                if (pieceInWay == null) {
                                    returnList.add(new ChessMove(myPosition, nextPosition, null));
                                }
                            }
                        }
                        //If black, and on seventh row (haven't moved yet) check for the next spot
                        if (this.pieceColor == ChessGame.TeamColor.BLACK) {
                            if (nextPosition.getRow() != 1) {
                                returnList.add(new ChessMove(myPosition, nextPosition, null));
                            }
                            else {
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
                                returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
                            }
                            //If white, and on seventh row (haven't moved yet) check for the next spot
                            if (myPosition.getRow() == 7) {
                                nextPosition = new ChessPosition(myPosition.getRow() + RowMoveDirection + RowMoveDirection, myPosition.getColumn());
                                pieceInWay = board.getPiece(nextPosition);
                                if (pieceInWay == null) {
                                    returnList.add(new ChessMove(myPosition, nextPosition, null));
                                }
                            }
                        }
                    }
                }
                //Check diagonal left
                nextRow = myPosition.getRow() + RowMoveDirection;
                nextCol = myPosition.getColumn() - 1;
                if (nextRow >= 1 && nextRow <= 8 && nextCol >= 1 && nextCol <= 8) {
                    nextPosition = new ChessPosition(nextRow, nextCol);
                    pieceInWay = board.getPiece(nextPosition);
                    if (pieceInWay != null) {
                        if (pieceInWay.getTeamColor() != this.pieceColor) {
                            if (this.pieceColor == ChessGame.TeamColor.WHITE) {
                                if (nextPosition.getRow() != 8) {
                                    returnList.add(new ChessMove(myPosition, nextPosition, null));
                                }
                                else{
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
                                }
                            }
                            if (this.pieceColor == ChessGame.TeamColor.BLACK) {
                                if (nextPosition.getRow() != 1) {
                                    returnList.add(new ChessMove(myPosition, nextPosition, null));
                                }
                                else{
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
                                }
                            }
                        }
                    }
                }
                //Check diagonal right
                nextRow = myPosition.getRow() + RowMoveDirection;
                nextCol = myPosition.getColumn() + 1;
                if (nextRow >= 1 && nextRow <= 8 && nextCol >= 1 && nextCol <= 8) {
                    nextPosition = new ChessPosition(nextRow, nextCol);
                    pieceInWay = board.getPiece(nextPosition);
                    if (pieceInWay != null) {
                        if (pieceInWay.getTeamColor() != this.pieceColor) {
                            if (this.pieceColor == ChessGame.TeamColor.WHITE) {
                                if (nextPosition.getRow() != 8) {
                                    returnList.add(new ChessMove(myPosition, nextPosition, null));
                                }
                                else{
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
                                }
                            }
                            if (this.pieceColor == ChessGame.TeamColor.BLACK) {
                                if (nextPosition.getRow() != 1) {
                                    returnList.add(new ChessMove(myPosition, nextPosition, null));
                                }
                                else{
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.BISHOP));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.KNIGHT));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.QUEEN));
                                    returnList.add(new ChessMove(myPosition, nextPosition, PieceType.ROOK));
                                }
                            }
                        }
                    }
                }


                return returnList;
            case QUEEN:
                //Up
                generateMoves(board, myPosition, myPosition, returnList, 1, 0, true);
                //UpRight
                generateMoves(board, myPosition, myPosition, returnList, 1, 1, true);
                //Right
                generateMoves(board, myPosition, myPosition, returnList, 0, 1, true);
                //RightDown
                generateMoves(board, myPosition, myPosition, returnList, -1, 1, true);
                //Down
                generateMoves(board, myPosition, myPosition, returnList, -1, 0, true);
                //DownLeft
                generateMoves(board, myPosition, myPosition, returnList, -1, -1, true);
                //Left
                generateMoves(board, myPosition, myPosition, returnList, 0, -1, true);
                //UpLeft
                generateMoves(board, myPosition, myPosition, returnList, 1, -1, true);
                return returnList;
            case ROOK:
                //Up
                generateMoves(board, myPosition, myPosition, returnList, 1, 0, true);
                //Right
                generateMoves(board, myPosition, myPosition, returnList, 0, 1, true);
                //Down
                generateMoves(board, myPosition, myPosition, returnList, -1, 0, true);
                //Left
                generateMoves(board, myPosition, myPosition, returnList, 0, -1, true);
                return returnList;
        }
        return List.of();
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
