package com.mvp.duelfire.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mvp.duelfire.domain.GameConstants

@Composable
fun WaitingScreen(
    state: DuelUiState,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    val duel = state.currentDuel
    val hasOpponent = duel?.players?.containsKey(GameConstants.PLAYER_2) == true
    val isHost = state.myPlayerId == GameConstants.PLAYER_1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DUEL FIRE", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(30.dp))
        if (isHost) {
            Text("Duel Code", color = Color.White.copy(alpha = 0.72f), fontSize = 18.sp)
            Text(state.duelCode, color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(18.dp))
            Text(
                text = if (hasOpponent) "Opponent connected" else "Waiting for opponent...",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onStart,
                enabled = hasOpponent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Battle")
            }
        } else {
            Text("Connected to duel", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("Waiting for host to start...", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
        }
        state.errorMessage?.let {
            Spacer(Modifier.height(14.dp))
            Text(it, color = Color(0xFFFF6B6B))
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
            Text("Exit")
        }
    }
}
