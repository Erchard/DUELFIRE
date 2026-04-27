package com.mvp.duelfire.domain

data class Duel(
    val duelCode: String = "",
    val status: String = GameConstants.STATUS_WAITING,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val winnerPlayerId: String? = null,
    val players: Map<String, Player> = emptyMap(),
    val lastEvent: DuelEvent = DuelEvent()
)

data class Player(
    val id: String = "",
    val name: String = "",
    val hp: Int = GameConstants.INITIAL_HP,
    val alive: Boolean = true,
    val connected: Boolean = true,
    val lastFireAt: Long = 0L
)

data class DuelEvent(
    val type: String = GameConstants.EVENT_NONE,
    val byPlayerId: String? = null,
    val targetPlayerId: String? = null,
    val damage: Int = 0,
    val timestamp: Long = 0L
)
