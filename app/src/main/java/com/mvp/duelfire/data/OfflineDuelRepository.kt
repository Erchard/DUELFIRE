package com.mvp.duelfire.data

import com.mvp.duelfire.domain.Duel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OfflineDuelRepository : DuelRepository {
    private val error = Result.failure<Unit>(
        IllegalStateException("Firebase is not configured. Add app/google-services.json or use Demo Mode.")
    )

    override suspend fun createDuel(playerName: String): Result<String> {
        return Result.failure(IllegalStateException("Firebase is not configured. Add app/google-services.json or use Demo Mode."))
    }

    override suspend fun joinDuel(code: String, playerName: String): Result<Unit> = error
    override suspend fun startBattle(code: String): Result<Unit> = error
    override suspend fun fire(code: String, myPlayerId: String): Result<Unit> = error
    override fun observeDuel(code: String): Flow<Duel?> = flowOf(null)
    override suspend fun resetDuel(code: String): Result<Unit> = error
    override suspend fun cancelDuel(code: String): Result<Unit> = error
}
