package client;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.EMPTY;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_BLACK;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_WHITE;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLACK;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class ChessClientHelper {
    public static String chessGameDisplay(ChessGame givenGame, Boolean isBlack, Boolean isWhite){
        StringBuilder boardBuilder = new StringBuilder();
        String rowString = "    a  b  c  d  e  f  g  h    ";
        int iStartingValue = 8; int iStoppingValue = 0; int iIncrementer = -1; int jStartingValue = 1; int jStoppingValue = 9; int jIncrementer = 1;
        if (isBlack && !isWhite){
            rowString = "    h  g  f  e  d  c  b  a    "; jStartingValue = 8; jStoppingValue = 0;
            jIncrementer = -1; iStartingValue = 1; iStoppingValue = 9; iIncrementer = 1;
        }
        boardBuilder.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLACK).append(rowString)
                .append(RESET_BG_COLOR).append("\n");

        for (int i = iStartingValue; i != iStoppingValue; i += iIncrementer) {
            boardBuilder
                    .append(SET_BG_COLOR_LIGHT_GREY)
                    .append(SET_TEXT_COLOR_BLACK)
                    .append(" ")
                    .append(i)
                    .append(" ")
                    .append(RESET_BG_COLOR);
            for (int j = jStartingValue; j != jStoppingValue; j += jIncrementer) {
                String squareColor = SET_BG_COLOR_WHITE;
                if ((i + j) % 2 == 0) {
                    squareColor = SET_BG_COLOR_BLACK;
                }
                boardBuilder
                        .append(squareColor)
                        .append(" ")
                        .append(drawPiece(givenGame.getBoard().getPiece(new ChessPosition(i, j))))
                        .append(" ");
            }
            boardBuilder
                    .append(SET_BG_COLOR_LIGHT_GREY)
                    .append(SET_TEXT_COLOR_BLACK)
                    .append(" ")
                    .append(i)
                    .append(" ")
                    .append(RESET_BG_COLOR)
                    .append("\n");
        }
        boardBuilder
                .append(SET_BG_COLOR_LIGHT_GREY)
                .append(SET_TEXT_COLOR_BLACK)
                .append(rowString)
                .append(RESET_BG_COLOR)
                .append("\n");
        return boardBuilder.toString();
    }
    public static String drawPiece(ChessPiece givenPiece){
        if (givenPiece == null)
        {
            return " ";
        }
        switch (givenPiece.getPieceType()){
            case KING:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "K";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "K";
                }
            case QUEEN:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "Q";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "Q";
                }
            case BISHOP:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "B";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "B";
                }
            case KNIGHT:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "N";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "N";
                }
            case ROOK:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "R";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "R";
                }
            case PAWN:
                if (givenPiece.getTeamColor().equals(ChessGame.TeamColor.WHITE)){
                    return SET_TEXT_COLOR_RED + "P";
                }
                else{
                    return SET_TEXT_COLOR_BLUE + "P";
                }
        }
        return EMPTY;
    }
    public static ChessPosition parsePositionString(String givenString){
        if (givenString.length() == 2){
            int col = -1;
            int row = -1;
            col = switch (givenString.charAt(0)) {
                case 'a' -> 1;
                case 'b' -> 2;
                case 'c' -> 3;
                case 'd' -> 4;
                case 'e' -> 5;
                case 'f' -> 6;
                case 'g' -> 7;
                case 'h' -> 8;
                default -> col;
            };
            row = switch (givenString.charAt(1)) {
                case '1' -> 1;
                case '2' -> 2;
                case '3' -> 3;
                case '4' -> 4;
                case '5' -> 5;
                case '6' -> 6;
                case '7' -> 7;
                case '8' -> 8;
                default -> row;
            };
            if (col != -1 && row != -1){
                return new ChessPosition(row, col);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        else{
            throw new IllegalArgumentException();
        }
    }
    public static ChessPiece.PieceType parsePromotionPiece(String givenString)
    {
        return switch (givenString) {
            case "pawn" -> ChessPiece.PieceType.PAWN;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "king" -> ChessPiece.PieceType.KING;
            default -> null;
        };
    }
}
