package com.mvp.duelfire.domain

/** Single place for shot rules (online and demo) so balance stays in sync. */
object DuelRules {
    fun enemyOf(playerId: String): String =
        if (playerId == GameConstants.PLAYER_1) GameConstants.PLAYER_2 else GameConstants.PLAYER_1

    /** Updated [Duel] after a valid shot, or null if the shot is not allowed. */
    fun resolvePlayerFire(duel: Duel, shooterId: String, now: Long): Duel? {
        if (duel.status != GameConstants.STATUS_ACTIVE) return null
        val enemyId = enemyOf(shooterId)
        val me = duel.players[shooterId] ?: return null
        val enemy = duel.players[enemyId] ?: return null
        if (!me.alive || !enemy.alive) return null
        if (now - me.lastFireAt < GameConstants.FIRE_COOLDOWN_MS) return null
        if (enemy.hp <= 0) return null

        val newEnemyHp = maxOf(0, enemy.hp - GameConstants.DAMAGE_PER_SHOT)
        val finished = newEnemyHp == 0
        val updatedPlayers = duel.players.toMutableMap().apply {
            put(shooterId, me.copy(lastFireAt = now))
            put(enemyId, enemy.copy(hp = newEnemyHp, alive = !finished))
        }
        return duel.copy(
            status = if (finished) GameConstants.STATUS_FINISHED else duel.status,
            updatedAt = now,
            winnerPlayerId = if (finished) shooterId else duel.winnerPlayerId,
            players = updatedPlayers,
            lastEvent = DuelEvent(
                type = if (finished) GameConstants.EVENT_VICTORY else GameConstants.EVENT_HIT,
                byPlayerId = shooterId,
                targetPlayerId = enemyId,
                damage = GameConstants.DAMAGE_PER_SHOT,
                timestamp = now
            )
        )
    }
}
