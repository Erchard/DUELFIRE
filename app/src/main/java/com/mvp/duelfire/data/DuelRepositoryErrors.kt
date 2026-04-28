package com.mvp.duelfire.data

/** No duel at this path. */
class DuelNotFoundException(message: String = "Duel not found.") : Exception(message)

/** Lobby already has two players. */
class DuelFullException(message: String = "This duel already has two players.") : Exception(message)

/** Duel is no longer in waiting state (started or finished). */
class DuelAlreadyStartedException(message: String = "This duel has already started.") : Exception(message)

/** Join transaction lost a race — user can retry. */
class DuelJoinRaceException(message: String = "Could not join. Please try again.") : Exception(message)

/** Battle cannot start until both players are present. */
class DuelStartNeedsOpponentException(
    message: String = "Start battle only when both players are connected."
) : Exception(message)
