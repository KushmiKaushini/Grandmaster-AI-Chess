package com.example.model

class ChessBoard {
    private val board: Array<Array<ChessPiece?>> = Array(8) { Array(8) { null } }

    init {
        setupInitialBoard()
    }

    fun getPiece(row: Int, col: Int): ChessPiece? {
        if (row !in 0..7 || col !in 0..7) return null
        return board[row][col]
    }

    fun setPiece(row: Int, col: Int, piece: ChessPiece?) {
        if (row in 0..7 && col in 0..7) {
            board[row][col] = piece
        }
    }

    fun copy(): ChessBoard {
        val newBoard = ChessBoard()
        for (r in 0..7) {
            for (c in 0..7) {
                newBoard.board[r][c] = this.board[r][c]
            }
        }
        return newBoard
    }

    private fun setupInitialBoard() {
        // Clear board first
        for (r in 0..7) {
            for (c in 0..7) {
                board[r][c] = null
            }
        }

        // Fill black back rank
        val backRankBlack = arrayOf(
            ChessPieceType.ROOK, ChessPieceType.KNIGHT, ChessPieceType.BISHOP,
            ChessPieceType.QUEEN, ChessPieceType.KING, ChessPieceType.BISHOP,
            ChessPieceType.KNIGHT, ChessPieceType.ROOK
        )
        for (c in 0..7) {
            board[0][c] = ChessPiece(backRankBlack[c], ChessColor.BLACK)
            board[1][c] = ChessPiece(ChessPieceType.PAWN, ChessColor.BLACK)
        }

        // Fill white back rank
        val backRankWhite = arrayOf(
            ChessPieceType.ROOK, ChessPieceType.KNIGHT, ChessPieceType.BISHOP,
            ChessPieceType.QUEEN, ChessPieceType.KING, ChessPieceType.BISHOP,
            ChessPieceType.KNIGHT, ChessPieceType.ROOK
        )
        for (c in 0..7) {
            board[6][c] = ChessPiece(ChessPieceType.PAWN, ChessColor.WHITE)
            board[7][c] = ChessPiece(backRankWhite[c], ChessColor.WHITE)
        }
    }

    // Pseudo-legal moves (without check verification to prevent recursion)
    fun getPseudoLegalMoves(row: Int, col: Int): List<Pair<Int, Int>> {
        val piece = getPiece(row, col) ?: return emptyList()
        val moves = mutableListOf<Pair<Int, Int>>()

        when (piece.type) {
            ChessPieceType.PAWN -> {
                val direction = if (piece.color == ChessColor.WHITE) -1 else 1
                val startRow = if (piece.color == ChessColor.WHITE) 6 else 1

                // 1 step forward
                val nextRow = row + direction
                if (nextRow in 0..7 && getPiece(nextRow, col) == null) {
                    moves.add(Pair(nextRow, col))

                    // 2 steps forward
                    val doubleRow = row + 2 * direction
                    if (row == startRow && doubleRow in 0..7 && getPiece(doubleRow, col) == null) {
                        moves.add(Pair(doubleRow, col))
                    }
                }

                // Captures
                val captureCols = listOf(col - 1, col + 1)
                for (c in captureCols) {
                    if (c in 0..7 && nextRow in 0..7) {
                        val targetPiece = getPiece(nextRow, c)
                        if (targetPiece != null && targetPiece.color != piece.color) {
                            moves.add(Pair(nextRow, c))
                        }
                    }
                }
            }

            ChessPieceType.KNIGHT -> {
                val knightOffsets = listOf(
                    Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                    Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
                )
                for (offset in knightOffsets) {
                    val r = row + offset.first
                    val c = col + offset.second
                    if (r in 0..7 && c in 0..7) {
                        val target = getPiece(r, c)
                        if (target == null || target.color != piece.color) {
                            moves.add(Pair(r, c))
                        }
                    }
                }
            }

            ChessPieceType.BISHOP -> {
                moves.addAll(getSlidingMoves(row, col, piece.color, listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1))))
            }

            ChessPieceType.ROOK -> {
                moves.addAll(getSlidingMoves(row, col, piece.color, listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1))))
            }

            ChessPieceType.QUEEN -> {
                moves.addAll(getSlidingMoves(row, col, piece.color, listOf(
                    Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1),
                    Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)
                )))
            }

            ChessPieceType.KING -> {
                val kingOffsets = listOf(
                    Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
                    Pair(0, -1),             Pair(0, 1),
                    Pair(1, -1),  Pair(1, 0),  Pair(1, 1)
                )
                for (offset in kingOffsets) {
                    val r = row + offset.first
                    val c = col + offset.second
                    if (r in 0..7 && c in 0..7) {
                        val target = getPiece(r, c)
                        if (target == null || target.color != piece.color) {
                            moves.add(Pair(r, c))
                        }
                    }
                }
            }
        }

        return moves
    }

    private fun getSlidingMoves(
        row: Int,
        col: Int,
        color: ChessColor,
        directions: List<Pair<Int, Int>>
    ): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (dir in directions) {
            var r = row + dir.first
            var c = col + dir.second
            while (r in 0..7 && c in 0..7) {
                val p = getPiece(r, c)
                if (p == null) {
                    moves.add(Pair(r, c))
                } else {
                    if (p.color != color) {
                        moves.add(Pair(r, c))
                    }
                    break
                }
                r += dir.first
                c += dir.second
            }
        }
        return moves
    }

    // Truly legal moves (taking into consideration if making the move leaves your King in Check)
    fun getLegalMoves(row: Int, col: Int): List<Pair<Int, Int>> {
        val piece = getPiece(row, col) ?: return emptyList()
        val pseudoMoves = getPseudoLegalMoves(row, col)
        val legalMoves = mutableListOf<Pair<Int, Int>>()

        for (move in pseudoMoves) {
            val tempBoard = this.copy()
            // Make simulated move
            val movingPiece = tempBoard.getPiece(row, col)
            tempBoard.setPiece(row, col, null)
            tempBoard.setPiece(move.first, move.second, movingPiece)

            if (!tempBoard.isKingUnderAttack(piece.color)) {
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    fun isKingUnderAttack(color: ChessColor): Boolean {
        // Find king position
        var kingRow = -1
        var kingCol = -1
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = getPiece(r, c)
                if (piece != null && piece.type == ChessPieceType.KING && piece.color == color) {
                    kingRow = r
                    kingCol = c
                    break
                }
            }
            if (kingRow != -1) break
        }

        // If king not found for some reason, return safe
        if (kingRow == -1) return false

        // Check if any opponent piece can attack king
        val opponentColor = color.opponent()
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = getPiece(r, c)
                if (piece != null && piece.color == opponentColor) {
                    val pseudoMoves = getPseudoLegalMoves(r, c)
                    if (pseudoMoves.any { it.first == kingRow && it.second == kingCol }) {
                        return true
                    }
                }
            }
        }

        return false
    }

    // Check if player has any legal moves
    fun hasAnyLegalMoves(color: ChessColor): Boolean {
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = getPiece(r, c)
                if (piece != null && piece.color == color) {
                    if (getLegalMoves(r, c).isNotEmpty()) {
                        return true
                    }
                }
            }
        }
        return false
    }

    // Helper to evaluate board value for local AI
    fun evaluateBoard(aiColor: ChessColor): Int {
        var score = 0
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = getPiece(r, c) ?: continue
                val pieceValue = piece.type.value
                val isAI = piece.color == aiColor
                
                // Base piece value
                val sign = if (isAI) 1 else -1
                score += sign * pieceValue

                // Positional bonuses (encouraging center control)
                // Center rows (3,4) & center cols (3,4) get a small bump, especially for pawns and knights
                if (piece.type == ChessPieceType.PAWN || piece.type == ChessPieceType.KNIGHT) {
                    val distToCenterRow = Math.abs(r - 3.5).toFloat()
                    val distToCenterCol = Math.abs(c - 3.5).toFloat()
                    val centerBonus = (7 - (distToCenterRow + distToCenterCol)) * 2
                    score += sign * centerBonus.toInt()
                }
            }
        }
        return score
    }

    // Perform a move
    fun makeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
        val piece = getPiece(fromRow, fromCol) ?: return false
        
        // Pawn promotion to Queen automatically for simplicity and clean flow
        val isPromotion = piece.type == ChessPieceType.PAWN && (toRow == 0 || toRow == 7)
        val finalPiece = if (isPromotion) ChessPiece(ChessPieceType.QUEEN, piece.color) else piece

        setPiece(fromRow, fromCol, null)
        setPiece(toRow, toCol, finalPiece)
        return true
    }

    // Export board as text grid for Gemini context
    fun toAsciiGrid(): String {
        val sb = StringBuilder()
        sb.append("  a b c d e f g h\n")
        for (r in 0..7) {
            sb.append("${8 - r} ")
            for (c in 0..7) {
                val piece = getPiece(r, c)
                if (piece == null) {
                    sb.append(". ")
                } else {
                    val symbol = piece.type.symbol
                    val letter = if (piece.color == ChessColor.WHITE) symbol.uppercase() else symbol.lowercase()
                    sb.append("$letter ")
                }
            }
            sb.append("${8 - r}\n")
        }
        sb.append("  a b c d e f g h\n")
        return sb.toString()
    }

    // Convert move positions to Standard Algebraic Notation (e.g., e2e4)
    fun getAlgebraicNotation(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): String {
        val files = "abcdefgh"
        val startFile = files[fromCol]
        val startRank = 8 - fromRow
        val endFile = files[toCol]
        val endRank = 8 - toRow
        return "$startFile$startRank$endFile$endRank"
    }

    // Move search (1-ply heuristic minimax search)
    fun findBestHeuristicMove(color: ChessColor, difficulty: String): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        val moves = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
        
        // Gather all legal moves
        for (r in 0..7) {
            for (c in 0..7) {
                val piece = getPiece(r, c)
                if (piece != null && piece.color == color) {
                    val legals = getLegalMoves(r, c)
                    for (m in legals) {
                        moves.add(Pair(Pair(r, c), Pair(m.first, m.second)))
                    }
                }
            }
        }

        if (moves.isEmpty()) return null

        // Easy: 70% random, 30% best
        // Medium: 30% random, 70% best 
        // Hard: 100% best
        val isRandom = when (difficulty.lowercase()) {
            "easy" -> Math.random() < 0.70
            "medium" -> Math.random() < 0.30
            else -> false
        }

        if (isRandom) {
            return moves.random()
        }

        // Evaluate and pick the highest scoring move
        var bestScore = Int.MIN_VALUE
        var bestMove = moves.random()

        for (move in moves) {
            val from = move.first
            val to = move.second
            val tempBoard = this.copy()
            val targetPiece = tempBoard.getPiece(to.first, to.second)
            
            // Add a small tactical bonus for capturing high-value pieces
            var tacticalBonus = 0
            if (targetPiece != null) {
                tacticalBonus = targetPiece.type.value * 10
            }

            tempBoard.makeMove(from.first, from.second, to.first, to.second)
            val score = tempBoard.evaluateBoard(color) + tacticalBonus
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        return bestMove
    }
}
