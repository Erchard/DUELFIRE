package com.mvp.duelfire.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mvp.duelfire.domain.Duel
import com.mvp.duelfire.domain.DuelEvent
import com.mvp.duelfire.domain.DuelRules
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
            if (tryCreateDuelAtomically(code, cleanName)) {
                return@runCatching code
            }
        }
        error("Could not generate a free duel code. Try again.")
    }

    override suspend fun joinDuel(code: String, playerName: String): Result<Unit> {
        val c = code.trim()
        val cleanName = playerName.ifBlank { "Player 2" }
        val tx = runDuelTransaction(c) { duel ->
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
        if (tx.isSuccess) return tx

        return try {
            val snap = duels.child(c).get().await()
            if (!snap.exists()) {
                Result.failure(DuelNotFoundException())
            } else {
                val duel = snap.getValue(Duel::class.java)
                if (duel == null) Result.failure(DuelNotFoundException())
                else classifyJoinFailure(duel)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun classifyJoinFailure(duel: Duel): Result<Unit> = when {
        duel.status != GameConstants.STATUS_WAITING ->
            Result.failure(DuelAlreadyStartedException())
        duel.players.containsKey(GameConstants.PLAYER_2) ->
            Result.failure(DuelFullException())
        else -> Result.failure(DuelJoinRaceException())
    }

    override suspend fun startBattle(code: String): Result<Unit> {
        val c = code.trim()
        val tx = runDuelTransaction(c) { duel ->
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
        if (tx.isSuccess) return tx

        return try {
            val snap = duels.child(c).get().await()
            if (!snap.exists()) {
                Result.failure(DuelNotFoundException())
            } else {
                val duel = snap.getValue(Duel::class.java)
                if (duel == null) Result.failure(DuelNotFoundException())
                else classifyStartFailure(duel)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun classifyStartFailure(duel: Duel): Result<Unit> {
        val hasBoth = duel.players.containsKey(GameConstants.PLAYER_1) &&
            duel.players.containsKey(GameConstants.PLAYER_2)
        return when {
            !hasBoth -> Result.failure(DuelStartNeedsOpponentException())
            duel.status != GameConstants.STATUS_WAITING ->
                Result.failure(DuelAlreadyStartedException())
            else -> Result.failure(DuelJoinRaceException())
        }
    }

    override suspend fun fire(code: String, myPlayerId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        return runDuelTransaction(code.trim()) { duel ->
            if (duel == null) return@runDuelTransaction null
            DuelRules.resolvePlayerFire(duel, myPlayerId, now)
        }
    }

    override fun observeDuel(code: String): Flow<Duel?> = callbackFlow {
        val ref = duels.child(code.trim())
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                trySend(snapshot.getValue(Duel::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "observeDuel cancelled: ${error.message}")
                trySend(null)
                close()
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
        duels.child(code.trim()).removeValue().await()
    }

    private suspend fun tryCreateDuelAtomically(code: String, cleanName: String): Boolean =
        suspendCoroutine { continuation ->
            duels.child(code).runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    if (currentData.getValue(Duel::class.java) != null) {
                        return Transaction.abort()
                    }
                    val now = System.currentTimeMillis()
                    currentData.value = Duel(
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
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                    when {
                        error != null -> continuation.resume(false)
                        else -> continuation.resume(committed)
                    }
                }
            })
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

    private companion object {
        private const val TAG = "FirebaseDuelRepo"
    }
}
