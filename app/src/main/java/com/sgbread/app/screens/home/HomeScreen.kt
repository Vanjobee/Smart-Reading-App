package com.sgbread.app.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.R
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.Modules
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SkyBlueLight

/** A tappable region on the home artwork, expressed as fractions (0f..1f) of the image size. */
private data class HotSpot(val left: Float, val top: Float, val right: Float, val bottom: Float)

private val LETTER_SPOT = HotSpot(0.090f, 0.545f, 0.225f, 0.790f)
private val PHONICS_SPOT = HotSpot(0.250f, 0.545f, 0.385f, 0.790f)
private val BLENDING_SPOT = HotSpot(0.415f, 0.545f, 0.550f, 0.790f)
private val DIGRAPHS_SPOT = HotSpot(0.575f, 0.545f, 0.710f, 0.790f)
private val SPEAKER_SPOT = HotSpot(0.010f, 0.015f, 0.085f, 0.125f)
private val PROFILE_SPOT = HotSpot(0.775f, 0.840f, 0.975f, 0.955f)

private const val IMAGE_ASPECT = 1536f / 1024f

@Composable
fun HomeScreen(
    progressViewModel: ProgressViewModel,
    audio: AudioManager,
    onModuleSelected: (String) -> Unit,
    onProfileSelected: () -> Unit
) {
    val progress by progressViewModel.state.collectAsStateWithLifecycle()
    val homeArt: Painter = painterResource(R.drawable.home)

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Matches the artwork's own sky-to-field horizon (~50%) so any
            // letterboxed margin on wide screens blends into the scene
            // instead of showing a flat color band or a duplicated crop.
            .background(
                Brush.verticalGradient(
                    0.0f to SkyBlueLight,
                    0.49f to SkyBlueLight,
                    0.51f to RiceGreenDark,
                    1.0f to RiceGreenDark
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenAspect = maxWidth / maxHeight
            val imageWidth: Dp
            val imageHeight: Dp
            if (screenAspect > IMAGE_ASPECT) {
                imageHeight = maxHeight
                imageWidth = maxHeight * IMAGE_ASPECT
            } else {
                imageWidth = maxWidth
                imageHeight = maxWidth / IMAGE_ASPECT
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(imageWidth, imageHeight)
            ) {
                Image(
                    painter = homeArt,
                    contentDescription = "SGB-READ farm home screen",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                fun hotspotModifier(spot: HotSpot, onClick: () -> Unit): Modifier = Modifier
                    .offset(x = imageWidth * spot.left, y = imageHeight * spot.top)
                    .size(imageWidth * (spot.right - spot.left), imageHeight * (spot.bottom - spot.top))
                    .clickable(onClick = onClick)

                Modules.all.forEachIndexed { index, module ->
                    val spot = when (index) {
                        0 -> LETTER_SPOT
                        1 -> PHONICS_SPOT
                        2 -> BLENDING_SPOT
                        else -> DIGRAPHS_SPOT
                    }
                    Box(modifier = hotspotModifier(spot) { onModuleSelected(module.id) })
                    if (progress.isModuleComplete(module.id)) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = imageWidth * spot.right - 18.dp,
                                    y = imageHeight * spot.top - 6.dp
                                )
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(CorrectGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Completed", tint = CreamWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Box(
                    modifier = hotspotModifier(SPEAKER_SPOT) {
                        audio.speak("Welcome to S G B Read! Grow your reading on the farm. Tap Letter, Phonics, Blending, or Digraphs to start.", rate = 0.9f)
                    }
                )
                Box(modifier = hotspotModifier(PROFILE_SPOT, onProfileSelected))
            }
        }
    }
}
