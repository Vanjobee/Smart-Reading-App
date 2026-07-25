package com.sgbread.app.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.R
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.ui.components.ArtworkHotspot
import com.sgbread.app.ui.components.ArtworkHotspotOverlay
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.SunYellow
import com.sgbread.app.ui.theme.TextBrown

private val HOME_HOTSPOTS = listOf(
    ArtworkHotspot("module1", "Open Letter module", 130f, 985f, 462f, 1332f),
    ArtworkHotspot("module2", "Open Phonics module", 475f, 985f, 808f, 1332f),
    ArtworkHotspot("module3", "Open Blending module", 130f, 1335f, 462f, 1670f),
    ArtworkHotspot("module4", "Open Digraphs module", 475f, 1335f, 808f, 1670f)
)

@Composable
fun HomeScreen(
    audio: AudioManager,
    onModuleSelected: (String) -> Unit,
    onProfileSelected: () -> Unit
) {
    val isAudioPlaying by audio.isPlaying.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        audio.replaceWithRawResource(R.raw.intro, key = "home-intro-autoplay")
    }
    DisposableEffect(audio) {
        onDispose { audio.stopPlayback() }
    }

    HomeScreenContent(
        selectorsEnabled = !isAudioPlaying,
        onPlayIntro = { audio.playRawResource(R.raw.intro, key = "home-intro") },
        onModuleSelected = onModuleSelected,
        onProfileSelected = onProfileSelected
    )
}

@Composable
private fun HomeScreenContent(
    selectorsEnabled: Boolean,
    onPlayIntro: () -> Unit,
    onModuleSelected: (String) -> Unit,
    onProfileSelected: () -> Unit
) {
    val homeArt: Painter = painterResource(R.drawable.home_portrait)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = homeArt,
            contentDescription = "SGB-READ farm home screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        ArtworkHotspotOverlay(
            hotspots = HOME_HOTSPOTS,
            enabled = selectorsEnabled,
            onSelected = onModuleSelected
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(SunYellow)
                .clickable(enabled = selectorsEnabled, onClick = onPlayIntro),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear instructions", tint = TextBrown)
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .clip(RoundedCornerShape(50))
                .background(CreamWhite.copy(alpha = 0.92f))
                .clickable(enabled = selectorsEnabled, onClick = onProfileSelected)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = TextBrown, modifier = Modifier.size(28.dp))
            Text("PROFILE", color = TextBrown, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SgbReadTheme {
        HomeScreenContent(
            selectorsEnabled = true,
            onPlayIntro = {},
            onModuleSelected = {},
            onProfileSelected = {}
        )
    }
}
