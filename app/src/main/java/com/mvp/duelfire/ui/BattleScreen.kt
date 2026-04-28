package com.mvp.duelfire.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvp.duelfire.domain.GameConstants
import com.mvp.duelfire.util.VibrationHelper

@Composable
fun BattleScreen(
    state: DuelUiState,
    onFire: () -> Unit,
    onReset: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val duel = state.currentDuel
    val myPlayer = duel?.players?.get(state.myPlayerId)
    val enemyPlayerId = if (state.myPlayerId == GameConstants.PLAYER_1) {
        GameConstants.PLAYER_2
    } else {
        GameConstants.PLAYER_1
    }
    val enemy = duel?.players?.get(enemyPlayerId)
    val finished = duel?.status == GameConstants.STATUS_FINISHED
    val cooldownRemainingMs = maxOf(0L, state.fireBlockedUntilMs - System.currentTimeMillis())
    val fireEnabled = duel?.status == GameConstants.STATUS_ACTIVE &&
        myPlayer?.alive == true &&
        enemy?.alive == true &&
        cooldownRemainingMs == 0L

    val background by animateColorAsState(
        targetValue = if (state.hitFlash) Color(0xFF3A1010) else Color(0xFF101014),
        label = "hit flash"
    )

    LaunchedEffect(state.hitFlash) {
        if (state.hitFlash) VibrationHelper.short(context, strong = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DUEL FIRE", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
                if (state.isDemoMode) {
                    Text("DEMO MODE", color = Color(0xFFFBBF24), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                HpBar(
                    label = "YOU",
                    hp = myPlayer?.hp ?: 0,
                    color = Color(0xFF22C55E)
                )
                Spacer(Modifier.height(30.dp))
                HpBar(
                    label = "ENEMY",
                    hp = enemy?.hp ?: 0,
                    color = Color(0xFFEF4444)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Status: ${state.statusMessage}",
                    color = statusColor(state.statusMessage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                if (cooldownRemainingMs > 0L && !finished) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Cooldown: ${((cooldownRemainingMs + 999L) / 1000L)}s",
                        color = Color(0xFFFBBF24),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                state.lastDamageText?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Color(0xFFFFD166), fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (finished) {
                    Text(
                        text = if (state.statusMessage == "VICTORY") "VICTORY" else "DEFEAT",
                        color = statusColor(state.statusMessage),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (state.statusMessage == "VICTORY") "Enemy eliminated" else "You were eliminated",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(18.dp))
                }

                Button(
                    onClick = {
                        VibrationHelper.short(context)
                        onFire()
                    },
                    enabled = fireEnabled,
                    modifier = Modifier
                        .widthIn(min = 220.dp)
                        .height(84.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("FIRE", fontSize = 30.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(18.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onReset, modifier = Modifier.size(width = 128.dp, height = 48.dp)) {
                        Text(if (finished) "Play Again" else "Reset")
                    }
                    OutlinedButton(onClick = onExit, modifier = Modifier.size(width = 96.dp, height = 48.dp)) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}

private fun statusColor(status: String): Color {
    return when (status) {
        "VICTORY" -> Color(0xFF22C55E)
        "DEFEAT", "YOU WERE HIT", "ERROR" -> Color(0xFFFF6B6B)
        "COOLDOWN" -> Color(0xFFFBBF24)
        "HIT" -> Color(0xFFFFD166)
        else -> Color.White
    }
}
