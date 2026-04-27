package com.mvp.duelfire.data

import com.mvp.duelfire.domain.Duel
import kotlinx.coroutines.flow.Flow

interface DuelRepository {
    suspend fun createDuel(playerName: String): Result<String>
    suspend fun joinDuel(code: String, playerName: String): Result<Unit>
    suspend fun startBattle(code: String): Result<Unit>
    suspend fun fire(code: String, myPlayerId: String): Result<Unit>
    fun observeDuel(code: String): Flow<Duel?>
    suspend fun resetDuel(code: String): Result<Unit>
    suspend fun cancelDuel(code: String): Result<Unit>
}
