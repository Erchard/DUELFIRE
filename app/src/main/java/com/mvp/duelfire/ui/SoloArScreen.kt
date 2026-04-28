package com.mvp.duelfire.ui

import android.graphics.Color
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.mvp.duelfire.domain.GameConstants
import com.mvp.duelfire.util.VibrationHelper
import io.github.sceneview.math.Position
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.math.Size
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader

@Composable
fun SoloArScreen(
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
    val placementKey = state.currentDuel?.createdAt ?: 0L
    val anchorReady = remember(placementKey) { mutableStateOf(false) }
    val fireEnabled = duel?.status == GameConstants.STATUS_ACTIVE &&
        myPlayer?.alive == true &&
        enemy?.alive == true &&
        cooldownRemainingMs == 0L &&
        anchorReady.value

    val background by animateColorAsState(
        targetValue = if (state.hitFlash) ComposeColor(0xFF3A1010) else ComposeColor(0x00000000),
        label = "hit flash"
    )

    LaunchedEffect(state.hitFlash) {
        if (state.hitFlash) VibrationHelper.short(context, strong = true)
    }

    val engine = rememberEngine()
    val materialLoader = rememberMaterialLoader(engine)
    val enemyMaterial = remember(materialLoader) {
        materialLoader.createColorInstance(
            Color.rgb(220, 38, 38),
            0.15f,
            0.45f,
            0.35f
        )
    }

    var enemyAnchor by remember(placementKey) { mutableStateOf<Anchor?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            planeRenderer = true,
            sessionConfiguration = { _: Session, config: Config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            },
            onSessionUpdated = { session, frame ->
                if (enemyAnchor == null) {
                    val plane = frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING && it.trackingState == TrackingState.TRACKING }
                    if (plane != null) {
                        enemyAnchor = session.createAnchor(plane.centerPose)
                    }
                }
                anchorReady.value = enemyAnchor != null &&
                    enemyAnchor?.trackingState == TrackingState.TRACKING
            }
        ) {
            enemyAnchor?.let { anchor ->
                AnchorNode(anchor = anchor) {
                    CubeNode(
                        size = Size(x = 0.28f, y = 0.42f, z = 0.22f),
                        materialInstance = enemyMaterial,
                        center = Position(x = 0f, y = 0.21f, z = 0f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val r = 18f
            val cross = androidx.compose.ui.graphics.Color.White
            drawCircle(
                color = cross.copy(alpha = 0.9f),
                radius = r,
                center = c,
                style = Stroke(width = 3f)
            )
            drawLine(
                color = cross.copy(alpha = 0.85f),
                start = Offset(c.x - r * 1.8f, c.y),
                end = Offset(c.x + r * 1.8f, c.y),
                strokeWidth = 2f
            )
            drawLine(
                color = cross.copy(alpha = 0.85f),
                start = Offset(c.x, c.y - r * 1.8f),
                end = Offset(c.x, c.y + r * 1.8f),
                strokeWidth = 2f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "DUEL FIRE",
                    color = ComposeColor.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "SOLO AR",
                    color = ComposeColor(0xFFFBBF24),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!anchorReady.value && !finished) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Наведіть на підлогу — з’явиться ворог",
                        color = ComposeColor.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                HpBar(
                    label = "YOU",
                    hp = myPlayer?.hp ?: 0,
                    color = ComposeColor(0xFF22C55E)
                )
                Spacer(Modifier.height(16.dp))
                HpBar(
                    label = "ENEMY",
                    hp = enemy?.hp ?: 0,
                    color = ComposeColor(0xFFEF4444)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Status: ${state.statusMessage}",
                    color = statusColorAr(state.statusMessage),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                if (cooldownRemainingMs > 0L && !finished) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Cooldown: ${((cooldownRemainingMs + 999L) / 1000L)}s",
                        color = ComposeColor(0xFFFBBF24),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                state.lastDamageText?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = ComposeColor(0xFFFFD166), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (finished) {
                    Text(
                        text = if (state.statusMessage == "VICTORY") "VICTORY" else "DEFEAT",
                        color = statusColorAr(state.statusMessage),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        VibrationHelper.short(context)
                        onFire()
                    },
                    enabled = fireEnabled,
                    modifier = Modifier
                        .widthIn(min = 200.dp)
                        .height(72.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(0xFFEF4444))
                ) {
                    Text("FIRE", fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onReset,
                        modifier = Modifier.size(width = 120.dp, height = 44.dp)
                    ) {
                        Text(if (finished) "Ще раз" else "Скинути")
                    }
                    OutlinedButton(
                        onClick = onExit,
                        modifier = Modifier.size(width = 88.dp, height = 44.dp)
                    ) {
                        Text("Вихід")
                    }
                }
            }
        }
    }
}

private fun statusColorAr(status: String): ComposeColor {
    return when (status) {
        "VICTORY" -> ComposeColor(0xFF22C55E)
        "DEFEAT", "YOU WERE HIT", "ERROR" -> ComposeColor(0xFFFF6B6B)
        "COOLDOWN" -> ComposeColor(0xFFFBBF24)
        "HIT" -> ComposeColor(0xFFFFD166)
        else -> ComposeColor.White
    }
}
