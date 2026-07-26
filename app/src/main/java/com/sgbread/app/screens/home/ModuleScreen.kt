package com.sgbread.app.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sgbread.app.R
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.ModuleInfo
import com.sgbread.app.ui.components.ArtworkHotspot
import com.sgbread.app.ui.components.ArtworkHotspotOverlay
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.TextBrown

private val MODE_1_HOTSPOTS = listOf(
    ArtworkHotspot("trace_letter", "Open Letter Trace", 205f, 925f, 735f, 1165f),
    ArtworkHotspot("letter_basket", "Open Letter Hunt", 190f, 1165f, 740f, 1395f),
    ArtworkHotspot("match_case", "Open Letter Match", 190f, 1395f, 740f, 1622f)
)

private val MODE_2_HOTSPOTS = listOf(
    ArtworkHotspot("listen_match", "Open Phonics Match", 145f, 830f, 795f, 1065f),
    ArtworkHotspot("tap_letter", "Open Tap Letter", 145f, 1065f, 795f, 1300f),
    ArtworkHotspot("letter_hunt", "Open Letter Hunt", 145f, 1300f, 795f, 1540f)
)

private val MODE_3_HOTSPOTS = listOf(
    ArtworkHotspot("missing_letter", "Open Fill in the Letter", 160f, 820f, 780f, 1225f),
    ArtworkHotspot("blend_read", "Open Blend and Read", 165f, 1225f, 780f, 1565f)
)

private val MODE_4_HOTSPOTS = listOf(
    ArtworkHotspot("digraph_build", "Open Digraph Sound", 75f, 675f, 630f, 1035f),
    ArtworkHotspot("picture_word_match", "Open Picture-to-Word Match", 60f, 1035f, 860f, 1338f),
    ArtworkHotspot("digraph_hunt", "Open Digraph Hunt", 170f, 1338f, 825f, 1610f)
)

@Composable
fun ModuleScreen(
    module: ModuleInfo,
    audio: AudioManager,
    onActivitySelected: (String) -> Unit,
    onProfileSelected: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(module.id) {
        audio.replaceWithRecordedPrompt(module.title)
    }
    DisposableEffect(audio, module.id) {
        onDispose { audio.stopPlayback() }
    }

    ModuleScreenContent(
        module = module,
        selectorsEnabled = true,
        onActivitySelected = { route ->
            audio.stopPlayback()
            onActivitySelected(route)
        },
        onProfileSelected = {
            audio.stopPlayback()
            onProfileSelected()
        },
        onBack = {
            audio.stopPlayback()
            onBack()
        }
    )
}

@Composable
private fun ModuleScreenContent(
    module: ModuleInfo,
    selectorsEnabled: Boolean,
    onActivitySelected: (String) -> Unit,
    onProfileSelected: () -> Unit,
    onBack: () -> Unit
) {
    val modeArtwork = when (module.number) {
        1 -> R.drawable.mode_1_portrait
        2 -> R.drawable.mode_2_portrait
        3 -> R.drawable.mode_3_portrait
        else -> R.drawable.mode_4_portrait
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(modeArtwork),
            contentDescription = "${module.title} activity selection",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )

        ArtworkHotspotOverlay(
            hotspots = when (module.number) {
                1 -> MODE_1_HOTSPOTS
                2 -> MODE_2_HOTSPOTS
                3 -> MODE_3_HOTSPOTS
                else -> MODE_4_HOTSPOTS
            },
            enabled = selectorsEnabled,
            onSelected = onActivitySelected
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(CreamWhite.copy(alpha = 0.88f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextBrown)
        }

        IconButton(
            onClick = onProfileSelected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(CreamWhite.copy(alpha = 0.88f))
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "Profile", tint = TextBrown)
        }

        if (module.number == 3) {
            Button(
                onClick = { onActivitySelected("blending_match") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 28.dp, vertical = 18.dp)
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RiceGreenDark,
                    contentColor = CreamWhite
                ),
                enabled = selectorsEnabled
            ) {
                Text(
                    "Blending Match",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun ModuleScreenPreview() {
    SgbReadTheme {
        ModuleScreenContent(
            module = com.sgbread.app.data.Modules.module4,
            selectorsEnabled = true,
            onActivitySelected = {},
            onProfileSelected = {},
            onBack = {}
        )
    }
}
