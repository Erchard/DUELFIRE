package com.mvp.duelfire.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mvp.duelfire.domain.GameConstants

@Composable
fun HpBar(
    label: String,
    hp: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val safeHp = hp.coerceIn(0, GameConstants.INITIAL_HP)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { safeHp / GameConstants.INITIAL_HP.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp),
            color = color,
            trackColor = Color(0xFF2A2A30)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$safeHp HP",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.86f)
        )
    }
}
