package com.example.model

enum class ChessGameStatus(val isGameOver: Boolean) {
    ACTIVE(false),
    CHECK_WHITE(false),
    CHECK_BLACK(false),
    CHECKMATE_WHITE_WINS(true),
    CHECKMATE_BLACK_WINS(true),
    STALEMATE(true)
}
