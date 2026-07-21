package com.sgbread.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            SgbNavGraph(
                navController = navController,
                progressViewModel = progressViewModel,
                audio = audioManager
            )
        }
    }
}
