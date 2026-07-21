package com.sgbread.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sgbread.app.ui.theme.TextBrown

/**
 * Common shell for every activity screen: farm background, a title bar with
 * back navigation and a "hear it again" speaker button, and a bottom-aligned
 * spot for the reinforcement banner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(
    title: String,
    onBack: () -> Unit,
    onReplayInstructions: (() -> Unit)?,
    feedback: AnswerFeedback = AnswerFeedback.None,
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FarmBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(title, color = TextBrown) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextBrown)
                        }
                    },
                    actions = {
                        if (onReplayInstructions != null) {
                            IconButton(onClick = onReplayInstructions) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear instructions", tint = TextBrown)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                content(PaddingValues())
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReinforcementBanner(feedback)
                }
            }
        }
    }
}
