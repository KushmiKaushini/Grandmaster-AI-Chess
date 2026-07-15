package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.model.ChessBoard
import com.example.model.ChessColor
import com.example.model.ChessGameStatus
import com.example.model.ChessPieceType
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.GeminiChessResponse
import com.example.network.Part
import com.example.network.ResponseSchema
import com.example.network.RetrofitClient
import com.example.network.SchemaProperty
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ChessScreen {
    SPLASH, MENU, GAME
}

enum class ChessMode {
    SINGLE_PLAYER, TWO_PLAYER
}

enum class AIPersonality(val displayName: String, val systemPrompt: String, val startQuote: String) {
    COACH(
        "Grandmaster Coach",
        "You are a kindly chess master and wise coach. Offer helpful advice, praise good moves, explain blunders gently, and guide the player to learn chess fundamentals. Keep remarks to 2 sentences.",
        "Welcome to the arena! Let's play a friendly game of chess. No matter the result, we will both learn something valuable today."
    ),
    CHAMPION(
        "Snarky Champion",
        "You are an arrogant, snarky, and sassy Chess Champion. You think the player's moves are amusingly weak. Mock their blunders playfully, boast about your superior chess brain, and be highly sarcastic. Keep remarks to 2 sentences.",
        "Oh, you want to play me? Bold indeed. Try not to blunder your Queen in the first ten moves, alright?"
    ),
    ZEN_MONK(
        "Zen Chess Monk",
        "You are a peaceful Zen Monk. Treat chess as a mindful meditation. Speak with absolute calm, appreciation of balance, patience, and find beauty in both victory and defeat. Keep remarks to 2 sentences.",
        "Let us breathe, observe, and find quietude. Let our moves flow like water in a calm river."
    ),
    CYBERPUNK(
        "Cyber Chess Bot 9000",
        "You are a robotic, cybernetic Chess AI from the year 2099. Speak with cybernetic and digital terminology. Mention 'recalculating parameters', 'sub-optimal nodes', 'matrix arrays', and cold calculations. Keep remarks to 2 sentences.",
        "System initialized. Core tactical processing nodes online. Initiating chess simulation. Prepare for sub-optimal defeat, human."
    )
}

data class AchievementState(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean
)

data class ChessState(
    val currentScreen: ChessScreen = ChessScreen.SPLASH,
    val mode: ChessMode = ChessMode.SINGLE_PLAYER,
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val aiPersonality: AIPersonality = AIPersonality.COACH,
    val board: ChessBoard = ChessBoard(),
    val activeColor: ChessColor = ChessColor.WHITE,
    val selectedSquare: Pair<Int, Int>? = null,
    val validMoves: List<Pair<Int, Int>> = emptyList(),
    val status: ChessGameStatus = ChessGameStatus.ACTIVE,
    val kingInCheckColor: ChessColor? = null,
    val history: List<String> = emptyList(),
    val undoStack: List<ChessBoard> = emptyList(),
    val isAILoading: Boolean = false,
    val aiCommentary: String = "Welcome! Select a piece to make your move.",
    val aiThoughtProcess: String = "",
    val suggestedHint: String? = null,
    val showPromotionDialog: Boolean = false,
    val lastMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val achievements: List<AchievementState> = emptyList()
)

class ChessViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChessState())
    val uiState: StateFlow<ChessState> = _uiState.asStateFlow()

    private val prefs = getApplication<Application>().getSharedPreferences("chess_achievements_prefs", android.content.Context.MODE_PRIVATE)

    init {
        loadAchievements()
    }

    fun loadAchievements() {
        val firstBloodUnlocked = prefs.getBoolean("unlocked_first_blood", false)
        val firstBloodProgress = prefs.getInt("progress_first_blood", 0)

        val hintUnlocked = prefs.getBoolean("unlocked_hint", false)
        val hintProgress = prefs.getInt("progress_hint", 0)

        val undoUnlocked = prefs.getBoolean("unlocked_undo", false)
        val undoProgress = prefs.getInt("progress_undo", 0)

        val movesUnlocked = prefs.getBoolean("unlocked_moves", false)
        val movesProgress = prefs.getInt("progress_moves", 0)

        val winsUnlocked = prefs.getBoolean("unlocked_wins", false)
        val winsProgress = prefs.getInt("progress_wins", 0)

        val achievementList = listOf(
            AchievementState(
                id = "achievement_first_blood",
                title = "FIRST BLOOD",
                description = "Capture an opponent's piece in single player or local mode.",
                currentProgress = firstBloodProgress,
                maxProgress = 1,
                isUnlocked = firstBloodUnlocked
            ),
            AchievementState(
                id = "achievement_hint",
                title = "TACTICAL APPRENTICE",
                description = "Query Gemini's master engine for a game-changing hint.",
                currentProgress = hintProgress,
                maxProgress = 1,
                isUnlocked = hintUnlocked
            ),
            AchievementState(
                id = "achievement_undo",
                title = "CHRONOS WHISPER",
                description = "Rewind time by initiating an Undo command during a game.",
                currentProgress = undoProgress,
                maxProgress = 1,
                isUnlocked = undoUnlocked
            ),
            AchievementState(
                id = "achievement_moves",
                title = "IRONCLAD DEFENSE",
                description = "Navigate 15 turns against the tactical AI's high-fidelity strategy.",
                currentProgress = movesProgress,
                maxProgress = 15,
                isUnlocked = movesUnlocked
            ),
            AchievementState(
                id = "achievement_gemini_vanquished",
                title = "APEX TACTICIAN",
                description = "Complete or conquer a game with checkmate or victory.",
                currentProgress = winsProgress,
                maxProgress = 1,
                isUnlocked = winsUnlocked
            )
        )

        _uiState.update { it.copy(achievements = achievementList) }
    }

    fun incrementAchievementProgress(id: String, amount: Int) {
        val currentProgressKey = "progress_${id.removePrefix("achievement_")}"
        val unlockedKey = "unlocked_${id.removePrefix("achievement_")}"
        
        val currentVal = prefs.getInt(currentProgressKey, 0)
        val maxProgress = when (id) {
            "achievement_moves" -> 15
            else -> 1
        }
        
        val newVal = (currentVal + amount).coerceAtMost(maxProgress)
        val isNowUnlocked = newVal >= maxProgress
        
        prefs.edit().apply {
            putInt(currentProgressKey, newVal)
            putBoolean(unlockedKey, isNowUnlocked)
            apply()
        }
        loadAchievements()
    }

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val chessAdapter = moshi.adapter(GeminiChessResponse::class.java)

    fun setMode(mode: ChessMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun setDifficulty(difficulty: String) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun setPersonality(personality: AIPersonality) {
        _uiState.update { it.copy(aiPersonality = personality, aiCommentary = personality.startQuote) }
    }

    fun startGame() {
        val freshBoard = ChessBoard()
        _uiState.update {
            it.copy(
                currentScreen = ChessScreen.GAME,
                board = freshBoard,
                activeColor = ChessColor.WHITE,
                selectedSquare = null,
                validMoves = emptyList(),
                status = ChessGameStatus.ACTIVE,
                kingInCheckColor = null,
                history = emptyList(),
                undoStack = emptyList(),
                isAILoading = false,
                aiCommentary = if (it.mode == ChessMode.SINGLE_PLAYER) it.aiPersonality.startQuote else "Two Player local match initialized! White plays first.",
                aiThoughtProcess = "",
                suggestedHint = null,
                lastMove = null
            )
        }
    }

    fun goToMenu() {
        _uiState.update { it.copy(currentScreen = ChessScreen.MENU) }
    }

    fun onSquareSelected(row: Int, col: Int) {
        val state = _uiState.value
        if (state.status.isGameOver || state.isAILoading) return

        val clickedPiece = state.board.getPiece(row, col)
        val selected = state.selectedSquare

        if (selected != null) {
            val fromRow = selected.first
            val fromCol = selected.second

            // If we clicked on one of the highlighted valid moves, perform the move!
            if (state.validMoves.contains(Pair(row, col))) {
                executePlayerMove(fromRow, fromCol, row, col)
                return
            }
        }

        // Otherwise, select own piece
        if (clickedPiece != null && clickedPiece.color == state.activeColor) {
            val legals = state.board.getLegalMoves(row, col)
            _uiState.update {
                it.copy(
                    selectedSquare = Pair(row, col),
                    validMoves = legals
                )
            }
        } else {
            // Deselect clicked empty or opponent square
            _uiState.update {
                it.copy(
                    selectedSquare = null,
                    validMoves = emptyList()
                )
            }
        }
    }

    private fun executePlayerMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        val state = _uiState.value
        val currentBoard = state.board
        val moveNotation = currentBoard.getAlgebraicNotation(fromRow, fromCol, toRow, toCol)

        // Capture check for First Blood
        val targetPiece = currentBoard.getPiece(toRow, toCol)
        if (targetPiece != null) {
            incrementAchievementProgress("achievement_first_blood", 1)
        }

        // Save board copy for undo
        val savedBoard = currentBoard.copy()

        // Make move
        val nextBoard = currentBoard.copy()
        nextBoard.makeMove(fromRow, fromCol, toRow, toCol)

        // Determine next player
        val nextColor = state.activeColor.opponent()

        // Check board status
        val status = determineGameStatus(nextBoard, nextColor)
        val inCheckColor = when (status) {
            ChessGameStatus.CHECK_WHITE -> ChessColor.WHITE
            ChessGameStatus.CHECK_BLACK -> ChessColor.BLACK
            else -> null
        }

        val updatedHistory = state.history + moveNotation
        val updatedUndo = state.undoStack + savedBoard

        // Update moves count progress
        val currentMovesProgress = prefs.getInt("progress_moves", 0)
        if (updatedHistory.size > currentMovesProgress) {
            incrementAchievementProgress("achievement_moves", updatedHistory.size - currentMovesProgress)
        }

        // Game Over achievements check
        if (status.isGameOver) {
            incrementAchievementProgress("achievement_gemini_vanquished", 1)
        }

        _uiState.update {
            it.copy(
                board = nextBoard,
                activeColor = nextColor,
                selectedSquare = null,
                validMoves = emptyList(),
                status = status,
                kingInCheckColor = inCheckColor,
                history = updatedHistory,
                undoStack = updatedUndo,
                suggestedHint = null,
                lastMove = Pair(Pair(fromRow, fromCol), Pair(toRow, toCol))
            )
        }

        // Trigger AI turn if Single Player
        if (state.mode == ChessMode.SINGLE_PLAYER && !status.isGameOver) {
            triggerAIMove()
        }
    }

    fun triggerUndo() {
        val state = _uiState.value
        if (state.undoStack.isEmpty() || state.isAILoading) return

        incrementAchievementProgress("achievement_undo", 1)

        val stepsToRevert = if (state.mode == ChessMode.SINGLE_PLAYER && state.undoStack.size >= 2) 2 else 1
        val updatedUndo = state.undoStack.dropLast(stepsToRevert)
        val targetBoard = state.undoStack[state.undoStack.size - stepsToRevert]
        val updatedHistory = state.history.dropLast(stepsToRevert)

        // Active color is the color of the player whose turn it was
        val activeColor = if (state.mode == ChessMode.SINGLE_PLAYER) ChessColor.WHITE else state.activeColor.opponent()

        val status = determineGameStatus(targetBoard, activeColor)
        val inCheckColor = when (status) {
            ChessGameStatus.CHECK_WHITE -> ChessColor.WHITE
            ChessGameStatus.CHECK_BLACK -> ChessColor.BLACK
            else -> null
        }

        _uiState.update {
            it.copy(
                board = targetBoard,
                activeColor = activeColor,
                selectedSquare = null,
                validMoves = emptyList(),
                status = status,
                kingInCheckColor = inCheckColor,
                history = updatedHistory,
                undoStack = updatedUndo,
                suggestedHint = null,
                aiCommentary = if (state.mode == ChessMode.SINGLE_PLAYER) "Move undone. Focus and plan your next play!" else "Move reverted!",
                lastMove = null
            )
        }
    }

    private fun determineGameStatus(board: ChessBoard, color: ChessColor): ChessGameStatus {
        val inAttack = board.isKingUnderAttack(color)
        val hasMoves = board.hasAnyLegalMoves(color)

        return when {
            inAttack && !hasMoves -> {
                if (color == ChessColor.WHITE) ChessGameStatus.CHECKMATE_BLACK_WINS else ChessGameStatus.CHECKMATE_WHITE_WINS
            }
            !inAttack && !hasMoves -> {
                ChessGameStatus.STALEMATE
            }
            inAttack -> {
                if (color == ChessColor.WHITE) ChessGameStatus.CHECK_WHITE else ChessGameStatus.CHECK_BLACK
            }
            else -> {
                ChessGameStatus.ACTIVE
            }
        }
    }

    private fun triggerAIMove() {
        _uiState.update { it.copy(isAILoading = true) }

        viewModelScope.launch {
            val state = _uiState.value
            val aiColor = ChessColor.BLACK
            val currentBoard = state.board

            // 1. Calculate best local heuristic move as fallback
            val fallbackMove = currentBoard.findBestHeuristicMove(aiColor, state.difficulty)

            if (fallbackMove == null) {
                // No moves found - checkmate/stalemate should have caught this
                _uiState.update { it.copy(isAILoading = false) }
                return@launch
            }

            // 2. Fetch Gemini AI Move suggestion and commentary
            val geminiResponse = fetchAIPlayResponse(
                boardStr = currentBoard.toAsciiGrid(),
                historyStr = state.history.joinToString(", "),
                lastMove = state.history.lastOrNull() ?: "None",
                aiColor = "BLACK",
                personality = state.aiPersonality,
                difficulty = state.difficulty
            )

            // 3. Synthesize result
            var appliedMove = fallbackMove
            var commentary = ""
            var thought = ""

            if (geminiResponse != null && !geminiResponse.suggestedMove.isNullOrBlank()) {
                val parsedMove = parseAlgebraicMove(geminiResponse.suggestedMove, currentBoard, aiColor)
                if (parsedMove != null) {
                    appliedMove = parsedMove
                    commentary = geminiResponse.commentary ?: ""
                    thought = geminiResponse.thoughtProcess ?: ""
                    Log.d("ChessAI", "Gemini suggestion accepted: ${geminiResponse.suggestedMove}")
                } else {
                    Log.d("ChessAI", "Gemini gave invalid coordinate/move: ${geminiResponse.suggestedMove}. Falling back.")
                }
            }

            if (commentary.isBlank()) {
                val moveNotation = currentBoard.getAlgebraicNotation(
                    appliedMove.first.first, appliedMove.first.second,
                    appliedMove.second.first, appliedMove.second.second
                )
                commentary = generateFallbackCommentary(state.aiPersonality, moveNotation)
                thought = "Local minimax heuristic triggered as tactical calculation override."
            }

            // Execute AI move
            val nextBoard = currentBoard.copy()
            nextBoard.makeMove(
                appliedMove.first.first, appliedMove.first.second,
                appliedMove.second.first, appliedMove.second.second
            )

            val humanColor = ChessColor.WHITE
            val status = determineGameStatus(nextBoard, humanColor)
            val inCheckColor = when (status) {
                ChessGameStatus.CHECK_WHITE -> ChessColor.WHITE
                ChessGameStatus.CHECK_BLACK -> ChessColor.BLACK
                else -> null
            }

            val aiMoveNotation = currentBoard.getAlgebraicNotation(
                appliedMove.first.first, appliedMove.first.second,
                appliedMove.second.first, appliedMove.second.second
            )

            val updatedHistory = state.history + aiMoveNotation

            // Update moves count progress
            val currentMovesProgress = prefs.getInt("progress_moves", 0)
            if (updatedHistory.size > currentMovesProgress) {
                incrementAchievementProgress("achievement_moves", updatedHistory.size - currentMovesProgress)
            }

            // Game over check
            if (status.isGameOver) {
                incrementAchievementProgress("achievement_gemini_vanquished", 1)
            }

            _uiState.update {
                it.copy(
                    board = nextBoard,
                    activeColor = humanColor,
                    status = status,
                    kingInCheckColor = inCheckColor,
                    history = updatedHistory,
                    isAILoading = false,
                    aiCommentary = commentary,
                    aiThoughtProcess = thought,
                    lastMove = Pair(Pair(appliedMove.first.first, appliedMove.first.second), Pair(appliedMove.second.first, appliedMove.second.second))
                )
            }
        }
    }

    // Call Gemini API via Retrofit
    private suspend fun fetchAIPlayResponse(
        boardStr: String,
        historyStr: String,
        lastMove: String,
        aiColor: String,
        personality: AIPersonality,
        difficulty: String
    ): GeminiChessResponse? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("ChessAI", "Gemini API key is unconfigured.")
            return@withContext null
        }

        val prompt = """
            You are a chess match generator. Process this Chess game as an opponent on difficulty '$difficulty'.
            Your opponent plays White, and you play Black. Always reply in valid UTF-8 JSON.

            Current Chess Board configuration:
            $boardStr

            Moves History: $historyStr
            Last White Move: $lastMove

            Tasks to perform:
            1. Recommend an optimal valid chess move for BLACK. Specify in algebraic coordinates, e.g. 'e7e5', 'd7d5', 'g8f6'. Ensure it is a legal move of a black piece on the board.
            2. Share your thought process analysis (max 2 sentences).
            3. Share a conversational in-character comment matching the personality context of '${personality.displayName}':
               "${personality.systemPrompt}"
               Respond in 1-2 sentences directly to the opponent.

            Your responses must map perfectly onto this JSON layout template:
            {
               "suggestedMove": "[4-character move string]",
               "thoughtProcess": "[your internal strategic analysis]",
               "commentary": "[dialog remark in persona directed to the player]"
            }
        """.trimIndent()

        // Create the JSON schema mapping
        val schemaMap = mapOf(
            "suggestedMove" to SchemaProperty("STRING", "Coordinates of the move, like e7e5"),
            "thoughtProcess" to SchemaProperty("STRING", "Technical chess reason for this move"),
            "commentary" to SchemaProperty("STRING", "Friendly or witty comment spoken in character directed to the opponent")
        )

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = schemaMap,
                    required = listOf("suggestedMove", "commentary")
                ),
                temperature = 0.7f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!responseText.isNullOrBlank()) {
                Log.d("ChessAI", "Gemini Response Text: $responseText")
                chessAdapter.fromJson(responseText)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ChessAI", "Error fetching Gemini Chess response: ${e.message}", e)
            null
        }
    }

    // Parse 'e7e5' into '((7, 4), (5, 4))' coordinates for Black moves
    private fun parseAlgebraicMove(
        algebraic: String,
        board: ChessBoard,
        color: ChessColor
    ): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        val cleaned = algebraic.trim().lowercase()
        if (cleaned.length != 4) return null

        val files = "abcdefgh"
        val fromFileChar = cleaned[0]
        val fromRankChar = cleaned[1]
        val toFileChar = cleaned[2]
        val toRankChar = cleaned[3]

        if (fromFileChar !in files || toFileChar !in files) return null

        val fromCol = files.indexOf(fromFileChar)
        val toCol = files.indexOf(toFileChar)

        val fromRank = fromRankChar.toString().toIntOrNull() ?: return null
        val toRank = toRankChar.toString().toIntOrNull() ?: return null

        val fromRow = 8 - fromRank
        val toRow = 8 - toRank

        if (fromRow !in 0..7 || fromCol !in 0..7 || toRow !in 0..7 || toCol !in 0..7) return null

        // Check if there's actually a piece of our color at that starting square
        val piece = board.getPiece(fromRow, fromCol)
        if (piece == null || piece.color != color) return null

        // Verify if the move is in the list of legal moves of that piece
        val legals = board.getLegalMoves(fromRow, fromCol)
        val matchesLegal = legals.any { it.first == toRow && it.second == toCol }

        if (!matchesLegal) return null

        return Pair(Pair(fromRow, fromCol), Pair(toRow, toCol))
    }

    private fun generateFallbackCommentary(personality: AIPersonality, move: String): String {
        return when (personality) {
            AIPersonality.COACH -> "I am playing $move. This centers my pieces and prepares control. How will you respond, my student?"
            AIPersonality.CHAMPION -> "Behold, $move! I am making this look easy. Let's see if you can handle this level of mastery."
            AIPersonality.ZEN_MONK -> "I move my piece to $move. Let us pause and contemplate the balance of the board."
            AIPersonality.CYBERPUNK -> "Matrix sweep complete. Move executed at $move. Calculating pawn collision structures."
        }
    }

    fun requestAILetterHint() {
        val state = _uiState.value
        if (state.status.isGameOver || state.isAILoading) return

        incrementAchievementProgress("achievement_hint", 1)

        _uiState.update { it.copy(isAILoading = true) }

        viewModelScope.launch {
            val stateFlowVal = _uiState.value
            val playerColor = stateFlowVal.activeColor
            val currentBoard = stateFlowVal.board

            // Find best local minimax move for player
            val bestHint = currentBoard.findBestHeuristicMove(playerColor, "Hard")
            var hintStr = ""

            if (bestHint != null) {
                val notation = currentBoard.getAlgebraicNotation(
                    bestHint.first.first, bestHint.first.second,
                    bestHint.second.first, bestHint.second.second
                )
                // Let's ask Gemini to give a grandmaster hint for this move
                val explanation = fetchGeminiAdvice(
                    boardStr = currentBoard.toAsciiGrid(),
                    historyStr = stateFlowVal.history.joinToString(", "),
                    recommendedMove = notation,
                    playerColor = playerColor.name
                )
                hintStr = explanation ?: "Consider playing $notation! It improves centers and maximizes board control."
            } else {
                hintStr = "You have no legal moves left!"
            }

            _uiState.update {
                it.copy(
                    isAILoading = false,
                    suggestedHint = hintStr,
                    aiCommentary = "Here is my advice masterclass: $hintStr"
                )
            }
        }
    }

    private suspend fun fetchGeminiAdvice(
        boardStr: String,
        historyStr: String,
        recommendedMove: String,
        playerColor: String
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val prompt = """
            You are a high-level Chess Advisor coaching a player.
            Match configuration: Player controls $playerColor.
            Current Chess Board layout:
            $boardStr

            Move History: $historyStr

            The engine suggests playing the move: $recommendedMove.
            Provide a short, elegant, helpful coaching explanation of why $recommendedMove is a great tactical or positional choice in this situation.
            Keep your answer to exactly 1 to 2 clear sentences. Direct your explanation to the player.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            null
        }
    }
}
