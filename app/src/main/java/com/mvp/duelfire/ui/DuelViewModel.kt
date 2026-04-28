package com.mvp.duelfire.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mvp.duelfire.data.DuelAlreadyStartedException
import com.mvp.duelfire.data.DuelFullException
import com.mvp.duelfire.data.DuelJoinRaceException
import com.mvp.duelfire.data.DuelNotFoundException
import com.mvp.duelfire.data.DuelRepository
import com.mvp.duelfire.data.DuelStartNeedsOpponentException
import com.mvp.duelfire.domain.Duel
import com.mvp.duelfire.domain.DuelRules
import com.mvp.duelfire.domain.GameConstants
import com.mvp.duelfire.domain.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScreenState {
    Start,
    Waiting,
    Battle
}

data class DuelUiState(
    val playerName: String = "",
    val joinCode: String = "",
    val duelCode: String = "",
    val myPlayerId: String = "",
    val currentDuel: Duel? = null,
    val screenState: ScreenState = ScreenState.Start,
    val statusMessage: String = "READY",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isDemoMode: Boolean = false,
    val lastDamageText: String? = null,
    val hitFlash: Boolean = false,
    val fireBlockedUntilMs: Long = 0L
)

class DuelViewModel(
    private val repository: DuelRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DuelUiState())
    val uiState: StateFlow<DuelUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var demoEnemyJob: Job? = null
    private var cooldownJob: Job? = null
    private var lastSeenEventTimestamp = 0L

    fun updatePlayerName(value: String) {
        _uiState.update { it.copy(playerName = value, errorMessage = null) }
    }

    fun updateJoinCode(value: String) {
        _uiState.update { it.copy(joinCode = value.filter(Char::isDigit).take(4), errorMessage = null) }
    }

    fun createDuel(playerName: String = uiState.value.playerName) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.createDuel(playerName.trim())
                .onSuccess { code ->
                    _uiState.update {
                        it.copy(
                            duelCode = code,
                            myPlayerId = GameConstants.PLAYER_1,
                            screenState = ScreenState.Waiting,
                            statusMessage = "WAITING",
                            isLoading = false,
                            isDemoMode = false
                        )
                    }
                    observeDuel(code)
                }
                .onFailure { showError(it.message ?: "Could not create duel.") }
        }
    }

    fun joinDuel(code: String = uiState.value.joinCode, playerName: String = uiState.value.playerName) {
        val cleanCode = code.trim()
        if (cleanCode.length != 4) {
            showError("Enter a 4 digit duel code.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.joinDuel(cleanCode, playerName.trim())
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            duelCode = cleanCode,
                            myPlayerId = GameConstants.PLAYER_2,
                            screenState = ScreenState.Waiting,
                            statusMessage = "WAITING",
                            isLoading = false,
                            isDemoMode = false
                        )
                    }
                    observeDuel(cleanCode)
                }
                .onFailure { e ->
                    showError(
                        when (e) {
                            is DuelNotFoundException -> e.message ?: "Duel not found."
                            is DuelFullException -> e.message ?: "This duel already has two players."
                            is DuelAlreadyStartedException -> e.message ?: "This duel has already started."
                            is DuelJoinRaceException -> e.message ?: "Could not join. Please try again."
                            else -> e.message ?: "Could not join duel."
                        }
                    )
                }
        }
    }

    fun startBattle() {
        val code = uiState.value.duelCode
        if (code.isBlank()) return
        viewModelScope.launch {
            repository.startBattle(code)
                .onFailure { e ->
                    showError(
                        when (e) {
                            is DuelNotFoundException -> e.message ?: "Duel not found."
                            is DuelStartNeedsOpponentException ->
                                e.message ?: "Start battle only when both players are connected."
                            is DuelAlreadyStartedException -> e.message ?: "This duel has already started."
                            is DuelJoinRaceException -> e.message ?: "Try again."
                            else -> e.message ?: "Could not start battle."
                        }
                    )
                }
        }
    }

    fun fire() {
        val state = uiState.value
        if (state.isDemoMode) {
            fireDemo()
            return
        }

        val code = state.duelCode
        val myPlayerId = state.myPlayerId
        if (code.isBlank() || myPlayerId.isBlank()) return

        viewModelScope.launch {
            repository.fire(code, myPlayerId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            statusMessage = "HIT",
                            lastDamageText = GameConstants.DAMAGE_SUMMARY,
                            fireBlockedUntilMs = System.currentTimeMillis() + GameConstants.FIRE_COOLDOWN_MS
                        )
                    }
                    clearDamageTextSoon()
                    scheduleReadyAfterCooldown()
                }
                .onFailure {
                    val message = if (isOnCooldown()) "COOLDOWN" else "WAITING"
                    _uiState.update {
                        it.copy(
                            statusMessage = message,
                            fireBlockedUntilMs = if (message == "COOLDOWN") cooldownUntilMs() else it.fireBlockedUntilMs
                        )
                    }
                    if (message == "COOLDOWN") scheduleReadyAfterCooldown()
                }
        }
    }

    fun resetDuel() {
        val state = uiState.value
        if (state.isDemoMode) {
            startDemoMode()
            return
        }
        val code = state.duelCode
        if (code.isBlank()) return
        viewModelScope.launch {
            repository.resetDuel(code)
                .onFailure { showError("Could not reset duel.") }
        }
    }

    fun exitDuel() {
        val snap = _uiState.value
        val code = snap.duelCode
        val demo = snap.isDemoMode
        observeJob?.cancel()
        demoEnemyJob?.cancel()
        cooldownJob?.cancel()
        if (!demo && code.isNotBlank() && code != "DEMO") {
            viewModelScope.launch {
                repository.cancelDuel(code)
            }
        }
        _uiState.value = DuelUiState(playerName = snap.playerName)
    }

    fun startDemoMode() {
        val now = System.currentTimeMillis()
        val duel = Duel(
            duelCode = "DEMO",
            status = GameConstants.STATUS_ACTIVE,
            createdAt = now,
            updatedAt = now,
            players = mapOf(
                GameConstants.PLAYER_1 to Player(id = GameConstants.PLAYER_1, name = uiState.value.playerName.ifBlank { "You" }),
                GameConstants.PLAYER_2 to Player(id = GameConstants.PLAYER_2, name = "Demo Enemy")
            )
        )
        observeJob?.cancel()
        demoEnemyJob?.cancel()
        _uiState.value = DuelUiState(
            playerName = uiState.value.playerName,
            duelCode = "DEMO",
            myPlayerId = GameConstants.PLAYER_1,
            currentDuel = duel,
            screenState = ScreenState.Battle,
            statusMessage = "READY",
            isDemoMode = true
        )
        demoEnemyJob = viewModelScope.launch {
            while (true) {
                delay(3000L)
                demoEnemyFire()
            }
        }
    }

    private fun observeDuel(code: String) {
        observeJob?.cancel()
        lastSeenEventTimestamp = 0L
        observeJob = viewModelScope.launch {
            repository.observeDuel(code).collect { duel ->
                if (duel == null) {
                    _uiState.update { prev ->
                        DuelUiState(
                            playerName = prev.playerName,
                            statusMessage = "READY",
                            errorMessage = "Duel ended or connection lost."
                        )
                    }
                    observeJob?.cancel()
                    return@collect
                }
                val myPlayerId = uiState.value.myPlayerId
                val screen = when (duel?.status) {
                    GameConstants.STATUS_ACTIVE, GameConstants.STATUS_FINISHED -> ScreenState.Battle
                    else -> ScreenState.Waiting
                }
                val message = statusFor(duel, myPlayerId)
                val wasHit = duel?.lastEvent?.targetPlayerId == myPlayerId &&
                    duel.lastEvent.timestamp > lastSeenEventTimestamp
                val wasOwnHit = duel?.lastEvent?.byPlayerId == myPlayerId &&
                    duel.lastEvent.type == GameConstants.EVENT_HIT &&
                    duel.lastEvent.timestamp > lastSeenEventTimestamp
                val fireBlockedUntilMs = duel?.players
                    ?.get(myPlayerId)
                    ?.lastFireAt
                    ?.plus(GameConstants.FIRE_COOLDOWN_MS)
                    ?.takeIf { it > System.currentTimeMillis() }
                    ?: 0L
                lastSeenEventTimestamp = maxOf(lastSeenEventTimestamp, duel?.lastEvent?.timestamp ?: 0L)

                _uiState.update {
                    it.copy(
                        currentDuel = duel,
                        screenState = screen,
                        statusMessage = when {
                            duel?.status == GameConstants.STATUS_FINISHED -> message
                            wasHit -> "YOU WERE HIT"
                            wasOwnHit -> "HIT"
                            else -> message
                        },
                        isLoading = false,
                        errorMessage = null,
                        hitFlash = wasHit,
                        lastDamageText = if (wasHit || wasOwnHit) GameConstants.DAMAGE_SUMMARY else it.lastDamageText,
                        fireBlockedUntilMs = fireBlockedUntilMs
                    )
                }
                if (wasHit) clearHitEffectsSoon()
                if (wasOwnHit) clearDamageTextSoon()
                if (fireBlockedUntilMs > 0L) scheduleReadyAfterCooldown(fireBlockedUntilMs)
            }
        }
    }

    private fun fireDemo() {
        val state = uiState.value
        val duel = state.currentDuel ?: return
        val me = duel.players[GameConstants.PLAYER_1] ?: return
        val now = System.currentTimeMillis()
        val updated = DuelRules.resolvePlayerFire(duel, GameConstants.PLAYER_1, now)
        if (updated == null) {
            _uiState.update {
                it.copy(
                    statusMessage = "COOLDOWN",
                    fireBlockedUntilMs = me.lastFireAt + GameConstants.FIRE_COOLDOWN_MS
                )
            }
            scheduleReadyAfterCooldown(me.lastFireAt + GameConstants.FIRE_COOLDOWN_MS)
            return
        }
        val finished = updated.status == GameConstants.STATUS_FINISHED
        _uiState.update {
            it.copy(
                currentDuel = updated,
                statusMessage = if (finished) statusFor(updated, it.myPlayerId) else "HIT",
                lastDamageText = GameConstants.DAMAGE_SUMMARY,
                fireBlockedUntilMs = if (finished) 0L else now + GameConstants.FIRE_COOLDOWN_MS
            )
        }
        clearDamageTextSoon()
        if (!finished) scheduleReadyAfterCooldown(now + GameConstants.FIRE_COOLDOWN_MS)
    }

    private fun demoEnemyFire() {
        val state = uiState.value
        if (!state.isDemoMode) return
        val duel = state.currentDuel ?: return
        if (duel.status != GameConstants.STATUS_ACTIVE) return
        val me = duel.players[GameConstants.PLAYER_1] ?: return
        val enemy = duel.players[GameConstants.PLAYER_2] ?: return
        if (!me.alive || !enemy.alive) return
        val now = System.currentTimeMillis()
        val updated = DuelRules.resolvePlayerFire(duel, GameConstants.PLAYER_2, now) ?: return
        val finished = updated.status == GameConstants.STATUS_FINISHED
        _uiState.update {
            it.copy(
                currentDuel = updated,
                statusMessage = if (finished) statusFor(updated, it.myPlayerId) else "YOU WERE HIT",
                hitFlash = true,
                lastDamageText = GameConstants.DAMAGE_SUMMARY
            )
        }
        clearHitEffectsSoon()
    }

    private fun statusFor(duel: Duel?, myPlayerId: String): String {
        if (duel == null) return "WAITING"
        if (duel.status == GameConstants.STATUS_FINISHED) {
            return if (duel.winnerPlayerId == myPlayerId) "VICTORY" else "DEFEAT"
        }
        return when (duel.status) {
            GameConstants.STATUS_ACTIVE -> if (isOnCooldown(duel, myPlayerId)) "COOLDOWN" else "READY"
            GameConstants.STATUS_WAITING -> "WAITING"
            else -> "ERROR"
        }
    }

    private fun isOnCooldown(
        duel: Duel? = uiState.value.currentDuel,
        myPlayerId: String = uiState.value.myPlayerId
    ): Boolean {
        val me = duel?.players?.get(myPlayerId) ?: return false
        return System.currentTimeMillis() - me.lastFireAt < GameConstants.FIRE_COOLDOWN_MS
    }

    private fun cooldownUntilMs(
        duel: Duel? = uiState.value.currentDuel,
        myPlayerId: String = uiState.value.myPlayerId
    ): Long {
        val lastFireAt = duel?.players?.get(myPlayerId)?.lastFireAt ?: return 0L
        return (lastFireAt + GameConstants.FIRE_COOLDOWN_MS).takeIf { it > System.currentTimeMillis() } ?: 0L
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, statusMessage = "ERROR", isLoading = false) }
    }

    private fun clearDamageTextSoon() {
        viewModelScope.launch {
            delay(1000L)
            _uiState.update { it.copy(lastDamageText = null) }
        }
    }

    private fun clearHitEffectsSoon() {
        viewModelScope.launch {
            delay(260L)
            _uiState.update { it.copy(hitFlash = false) }
            delay(740L)
            _uiState.update {
                val shouldResetHitStatus = it.statusMessage == "YOU WERE HIT" &&
                    it.currentDuel?.status == GameConstants.STATUS_ACTIVE
                it.copy(
                    lastDamageText = null,
                    statusMessage = if (shouldResetHitStatus) {
                        statusFor(it.currentDuel, it.myPlayerId)
                    } else {
                        it.statusMessage
                    }
                )
            }
        }
    }

    private fun scheduleReadyAfterCooldown(untilMs: Long = uiState.value.fireBlockedUntilMs) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            delay(maxOf(0L, untilMs - System.currentTimeMillis()))
            _uiState.update {
                if (it.currentDuel?.status == GameConstants.STATUS_ACTIVE) {
                    it.copy(
                        statusMessage = statusFor(it.currentDuel, it.myPlayerId),
                        fireBlockedUntilMs = 0L
                    )
                } else {
                    it.copy(fireBlockedUntilMs = 0L)
                }
            }
        }
    }
}
