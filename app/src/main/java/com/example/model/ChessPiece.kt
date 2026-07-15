package com.example.model

enum class ChessColor {
    WHITE, BLACK;

    fun opponent() = if (this == WHITE) BLACK else WHITE
}

enum class ChessPieceType(val symbol: String, val value: Int) {
    PAWN("P", 10),
    KNIGHT("N", 30),
    BISHOP("B", 30),
    ROOK("R", 50),
    QUEEN("Q", 90),
    KING("K", 1000)
}

data class ChessPiece(
    val type: ChessPieceType,
    val color: ChessColor
) {
    fun getUnicodeSymbol(): String {
        return when (color) {
            ChessColor.WHITE -> when (type) {
                ChessPieceType.KING -> "♔"
                ChessPieceType.QUEEN -> "♕"
                ChessPieceType.ROOK -> "♖"
                ChessPieceType.BISHOP -> "♗"
                ChessPieceType.KNIGHT -> "♘"
                ChessPieceType.PAWN -> "♙"
            }
            ChessColor.BLACK -> when (type) {
                ChessPieceType.KING -> "♚"
                ChessPieceType.QUEEN -> "♛"
                ChessPieceType.ROOK -> "♜"
                ChessPieceType.BISHOP -> "♝"
                ChessPieceType.KNIGHT -> "♞"
                ChessPieceType.PAWN -> "♟"
            }
        }
    }
}
