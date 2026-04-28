package com.mvp.duelfire.data

import com.mvp.duelfire.domain.Duel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * @param userHint показується в повідомленнях про помилку (наприклад, немає RTDB URL у google-services).
 */
class OfflineDuelRepository(
    private val userHint: String = "Firebase is not configured. Add app/google-services.json from Firebase Console or use Demo Mode."
) : DuelRepository {
    private val failure: Result<Unit> = Result.failure(IllegalStateException(userHint))

    override suspend fun createDuel(playerName: String): Result<String> =
        Result.failure(IllegalStateException(userHint))

    override suspend fun joinDuel(code: String, playerName: String): Result<Unit> = failure
    override suspend fun startBattle(code: String): Result<Unit> = failure
    override suspend fun fire(code: String, myPlayerId: String): Result<Unit> = failure
    override fun observeDuel(code: String): Flow<Duel?> = flowOf(null)
    override suspend fun resetDuel(code: String): Result<Unit> = failure
    override suspend fun cancelDuel(code: String): Result<Unit> = failure
}
