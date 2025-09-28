package chess;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] squares = new ChessPiece[9][9];

    public ChessBoard() {

    }
    public ChessBoard(ChessBoard copyBoard){
        squares = new ChessPiece[9][9];
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++) {
                ChessPosition positionToSet = new ChessPosition(i,j);
                ChessPiece pieceToPlace = copyBoard.getPiece(positionToSet);
                this.addPiece(positionToSet, pieceToPlace);
            }
        }
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        return squares[position.getRow()][position.getColumn()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[9][9];

        //Add all pawns
        for (int i = 1; i <= 8; i++){
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        //Add Rooks
        addPiece(new ChessPosition(1,1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        //Add Knights
        addPiece(new ChessPosition(1,2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        //Add Bishops
        addPiece(new ChessPosition(1,3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        //Add Kings and Queens
        addPiece(new ChessPosition(1,4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8,4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
    }

    public boolean CheckForCheck(ChessGame.TeamColor teamColor) {
        ChessPosition kingPosition = null;
        for (int i = 1; i <= 8; i++ )
        {
            for (int j = 1; j <= 8; j++)
            {
                ChessPosition searchPosition = new ChessPosition(i, j);
                if (this.getPiece(searchPosition) != null) {
                    if (this.getPiece(searchPosition).getPieceType() == ChessPiece.PieceType.KING) {
                        if (this.getPiece(searchPosition).getTeamColor() == teamColor) {
                            kingPosition = searchPosition;
                        }
                    }
                }
            }
        }
        HashSet<ChessMove> allMoves = (HashSet<ChessMove>) getAllMoves();
        for (ChessMove move : allMoves){
            if (move.getEndPosition().equals(kingPosition)){
                return true;
            }
        }
        return false;
    }


    public boolean CheckForCheckMate(ChessGame.TeamColor teamColor) {
        boolean inCheckmate = true;
        HashSet<ChessMove> allPossibleMoves = (HashSet<ChessMove>) getAllMoves();
        for (ChessMove move : allPossibleMoves){
            if (this.getPiece(move.getStartPosition()).getTeamColor() == teamColor) {
                ChessBoard nextGameState = new ChessBoard(this);
                nextGameState.MakeMove(move);
                if (!nextGameState.CheckForCheck(teamColor)) {
                    inCheckmate = false;
                }
            }
        }
        return inCheckmate;
    }

    public boolean CheckForStalemate(ChessGame.TeamColor teamColor) {
        if (CheckForCheck(teamColor)) {
            return false;
        }
        return CheckForCheckMate(teamColor);
    }

    public void MakeMove(ChessMove move)
    {
        ChessPiece.PieceType movePieceType = this.getPiece(move.getStartPosition()).getPieceType();
        ChessGame.TeamColor movePieceColor = this.getPiece(move.getStartPosition()).getTeamColor();
        ChessPiece movePiece;
        if (move.getPromotionPiece() == null) {
            movePiece = new ChessPiece(movePieceColor, movePieceType);
        }
        else{
            movePiece = new ChessPiece(movePieceColor, move.getPromotionPiece());
        }
        this.addPiece(move.getEndPosition(), null);
        this.addPiece(move.getStartPosition(), null);
        this.addPiece(move.getEndPosition(), movePiece);
    }

    public boolean moveIsValid(ChessMove move, ChessGame.TeamColor teamColor)
    {
        ChessBoard testBoard = new ChessBoard(this);
        testBoard.MakeMove(move);
        return !testBoard.CheckForCheck(teamColor);
    }

    public Collection<ChessMove> getAllMoves() {
        HashSet<ChessMove> returnList = new HashSet<>();
        for (int i = 1; i <= 8; i++ )
        {
            for (int j = 1; j <= 8; j++)
            {
                ChessPosition searchPosition = new ChessPosition(i, j);
                if (this.getPiece(searchPosition) != null) {
                    HashSet<ChessMove> currentPieceList = (HashSet<ChessMove>) this.getPiece(searchPosition).pieceMoves(this, searchPosition);
                    returnList.addAll(currentPieceList);
                }
            }
        }
        return returnList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
//    @Override
//    public ChessBoard clone(){
//        ChessBoard returnBoard = new ChessBoard();
//        for (int i = 1; i <= 8; i++){
//            for (int j = 1; j <= 8; j++) {
//                ChessPosition positionToSet = new ChessPosition(i,j);
//                ChessPiece pieceToPlace = this.getPiece(positionToSet);
//                returnBoard.addPiece(positionToSet, pieceToPlace);
//            }
//        }
//        return returnBoard;
//    }
}
