package com.mvp.duelfire.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mvp.duelfire.domain.Duel
import com.mvp.duelfire.domain.DuelEvent
import com.mvp.duelfire.domain.GameConstants
import com.mvp.duelfire.domain.Player
import com.mvp.duelfire.util.CodeGenerator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDuelRepository : DuelRepository {
    private val duels = Firebase.database.reference.child("duels")

    override suspend fun createDuel(playerName: String): Result<String> = runCatching {
        val cleanName = playerName.ifBlank { "Player 1" }
        repeat(20) {
            val code = CodeGenerator.fourDigitCode()
            val ref = duels.child(code)
            if (!ref.get().await().exists()) {
                val now = System.currentTimeMillis()
                ref.setValue(
                    Duel(
                        duelCode = code,
                        status = GameConstants.STATUS_WAITING,
                        createdAt = now,
                        updatedAt = now,
                        players = mapOf(
                            GameConstants.PLAYER_1 to Player(
                                id = GameConstants.PLAYER_1,
                                name = cleanName
                            )
                        )
                    )
                ).await()
                return@runCatching code
            }
        }
        error("Could not generate a free duel code. Try again.")
    }

    override suspend fun joinDuel(code: String, playerName: String): Result<Unit> {
        val cleanName = playerName.ifBlank { "Player 2" }
        return runDuelTransaction(code.trim()) { duel ->
            if (duel == null) return@runDuelTransaction null
            if (duel.status != GameConstants.STATUS_WAITING) return@runDuelTransaction null
            if (duel.players.containsKey(GameConstants.PLAYER_2)) return@runDuelTransaction null

            duel.copy(
                updatedAt = System.currentTimeMillis(),
                players = duel.players + (
                    GameConstants.PLAYER_2 to Player(
                        id = GameConstants.PLAYER_2,
                        name = cleanName
                    )
                )
            )
        }
    }

    override suspend fun startBattle(code: String): Result<Unit> {
        return runDuelTransaction(code.trim()) { duel ->
            if (duel == null) return@runDuelTransaction null
            val hasBothPlayers = duel.players.containsKey(GameConstants.PLAYER_1) &&
                duel.players.containsKey(GameConstants.PLAYER_2)
            if (duel.status != GameConstants.STATUS_WAITING || !hasBothPlayers) {
                return@runDuelTransaction null
            }

            duel.copy(
                status = GameConstants.STATUS_ACTIVE,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun fire(code: String, myPlayerId: String): Result<Unit> {
        return runDuelTransaction(code.trim()) { duel ->
            if (duel == null) return@runDuelTransaction null
            val enemyPlayerId = enemyOf(myPlayerId)
            val me = duel.players[myPlayerId] ?: return@runDuelTransaction null
            val enemy = duel.players[enemyPlayerId] ?: return@runDuelTransaction null
            val now = System.currentTimeMillis()

            if (
                duel.status != GameConstants.STATUS_ACTIVE ||
                !me.alive ||
                !enemy.alive ||
                now - me.lastFireAt < GameConstants.FIRE_COOLDOWN_MS ||
                enemy.hp <= 0
            ) {
                return@runDuelTransaction null
            }

            val newEnemyHp = maxOf(0, enemy.hp - GameConstants.DAMAGE_PER_SHOT)
            val finished = newEnemyHp == 0
            val updatedPlayers = duel.players.toMutableMap().apply {
                put(myPlayerId, me.copy(lastFireAt = now))
                put(enemyPlayerId, enemy.copy(hp = newEnemyHp, alive = !finished))
            }

            duel.copy(
                status = if (finished) GameConstants.STATUS_FINISHED else duel.status,
                updatedAt = now,
                winnerPlayerId = if (finished) myPlayerId else duel.winnerPlayerId,
                players = updatedPlayers,
                lastEvent = DuelEvent(
                    type = if (finished) GameConstants.EVENT_VICTORY else GameConstants.EVENT_HIT,
                    byPlayerId = myPlayerId,
                    targetPlayerId = enemyPlayerId,
                    damage = GameConstants.DAMAGE_PER_SHOT,
                    timestamp = now
                )
            )
        }
    }

    override fun observeDuel(code: String): Flow<Duel?> = callbackFlow {
        val ref = duels.child(code.trim())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Duel::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun resetDuel(code: String): Result<Unit> {
        return runDuelTransaction(code.trim()) { duel ->
            if (duel == null) return@runDuelTransaction null
            val now = System.currentTimeMillis()
            val resetPlayers = duel.players.mapValues { (_, player) ->
                player.copy(
                    hp = GameConstants.INITIAL_HP,
                    alive = true,
                    connected = true,
                    lastFireAt = 0L
                )
            }

            duel.copy(
                status = if (resetPlayers.size == 2) GameConstants.STATUS_ACTIVE else GameConstants.STATUS_WAITING,
                updatedAt = now,
                winnerPlayerId = null,
                players = resetPlayers,
                lastEvent = DuelEvent(type = GameConstants.EVENT_RESET, timestamp = now)
            )
        }
    }

    override suspend fun cancelDuel(code: String): Result<Unit> = runCatching {
        duels.child(code.trim()).child("status").setValue(GameConstants.STATUS_CANCELLED).await()
    }

    private suspend fun runDuelTransaction(
        code: String,
        update: (Duel?) -> Duel?
    ): Result<Unit> = suspendCoroutine { continuation ->
        duels.child(code).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val updated = update(currentData.getValue(Duel::class.java))
                    ?: return Transaction.abort()
                currentData.value = updated
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                when {
                    error != null -> continuation.resume(Result.failure(error.toException()))
                    !committed -> continuation.resume(Result.failure(IllegalStateException("Action is not available now.")))
                    else -> continuation.resume(Result.success(Unit))
                }
            }
        })
    }

    private fun enemyOf(playerId: String): String {
        return if (playerId == GameConstants.PLAYER_1) GameConstants.PLAYER_2 else GameConstants.PLAYER_1
    }
}
