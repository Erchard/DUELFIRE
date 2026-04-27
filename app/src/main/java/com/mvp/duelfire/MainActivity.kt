package com.mvp.duelfire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.mvp.duelfire.data.DuelRepository
import com.mvp.duelfire.data.FirebaseDuelRepository
import com.mvp.duelfire.data.OfflineDuelRepository
import com.mvp.duelfire.ui.BattleScreen
import com.mvp.duelfire.ui.DuelViewModel
import com.mvp.duelfire.ui.ScreenState
import com.mvp.duelfire.ui.StartScreen
import com.mvp.duelfire.ui.WaitingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = createRepository()
        setContent {
            DuelFireApp(repository)
        }
    }

    private fun createRepository(): DuelRepository {
        return try {
            if (FirebaseApp.initializeApp(this) == null) {
                OfflineDuelRepository()
            } else {
                FirebaseDuelRepository()
            }
        } catch (_: IllegalStateException) {
            OfflineDuelRepository()
        }
    }
}

@Composable
private fun DuelFireApp(repository: DuelRepository) {
    val viewModel: DuelViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DuelViewModel(repository) as T
            }
        }
    )
    val state by viewModel.uiState.collectAsState()

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFEF4444),
            background = Color(0xFF101014),
            surface = Color(0xFF18181D),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            error = Color(0xFFFF6B6B)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101014))
        ) {
            when (state.screenState) {
                ScreenState.Start -> StartScreen(
                    state = state,
                    onNameChange = viewModel::updatePlayerName,
                    onCodeChange = viewModel::updateJoinCode,
                    onCreate = { viewModel.createDuel() },
                    onJoin = { viewModel.joinDuel() },
                    onDemo = viewModel::startDemoMode
                )

                ScreenState.Waiting -> WaitingScreen(
                    state = state,
                    onStart = viewModel::startBattle,
                    onExit = viewModel::exitDuel
                )

                ScreenState.Battle -> BattleScreen(
                    state = state,
                    onFire = viewModel::fire,
                    onReset = viewModel::resetDuel,
                    onExit = viewModel::exitDuel
                )
            }
        }
    }
}
