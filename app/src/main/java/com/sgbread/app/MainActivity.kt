package com.sgbread.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sgbread.app.audio.rememberAudioManager
import com.sgbread.app.navigation.SgbNavGraph
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.ui.theme.SgbReadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SgbReadApp()
        }
    }
}

@Composable
private fun SgbReadApp() {
    SgbReadTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val progressViewModel: ProgressViewModel = viewModel()
            val audioManager = rememberAudioManager()
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(audioManager, lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> audioManager.startBackgroundMusic()
                        Lifecycle.Event.ON_STOP -> audioManager.pauseBackgroundMusic()
                        Lifecycle.Event.ON_DESTROY -> audioManager.stopBackgroundMusic()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                audioManager.startBackgroundMusic()
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    audioManager.stopBackgroundMusic()
                }
            }
            SgbNavGraph(
                navController = navController,
                progressViewModel = progressViewModel,
                audio = audioManager
            )
        }
    }
}
