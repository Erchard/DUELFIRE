package com.mvp.duelfire.domain

object GameConstants {
    const val INITIAL_HP = 100
    const val DAMAGE_PER_SHOT = 25
    const val FIRE_COOLDOWN_MS = 2000L

    /** UI text after a hit (kept in sync with [DAMAGE_PER_SHOT]). */
    val DAMAGE_SUMMARY: String get() = "-$DAMAGE_PER_SHOT HP"

    const val STATUS_WAITING = "waiting"
    const val STATUS_ACTIVE = "active"
    const val STATUS_FINISHED = "finished"
    const val STATUS_CANCELLED = "cancelled"

    const val PLAYER_1 = "player1"
    const val PLAYER_2 = "player2"

    const val EVENT_NONE = "none"
    const val EVENT_HIT = "hit"
    const val EVENT_VICTORY = "victory"
    const val EVENT_RESET = "reset"
}
