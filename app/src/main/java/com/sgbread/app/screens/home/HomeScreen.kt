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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.R
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.Modules
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.SunYellow
import com.sgbread.app.ui.theme.TextBrown

/** A tappable region on the home artwork, expressed as fractions (0f..1f) of the image size. */
private data class HotSpot(val left: Float, val top: Float, val right: Float, val bottom: Float)

private val LETTER_SPOT = HotSpot(0.090f, 0.545f, 0.225f, 0.790f)
private val PHONICS_SPOT = HotSpot(0.250f, 0.545f, 0.385f, 0.790f)
private val BLENDING_SPOT = HotSpot(0.415f, 0.545f, 0.550f, 0.790f)
private val DIGRAPHS_SPOT = HotSpot(0.575f, 0.545f, 0.710f, 0.790f)
private val PROFILE_SPOT = HotSpot(0.775f, 0.840f, 0.975f, 0.955f)

private const val IMAGE_ASPECT = 1536f / 1024f
private const val WELCOME_MESSAGE =
    "Welcome to S G B Read! Grow your reading on the farm. Tap Letter, Phonics, Blending, or Digraphs to start."

@Composable
fun HomeScreen(
    progressViewModel: ProgressViewModel,
    audio: AudioManager,
    onModuleSelected: (String) -> Unit,
    onProfileSelected: () -> Unit
) {
    val progress by progressViewModel.state.collectAsStateWithLifecycle()
    val homeArt: Painter = painterResource(R.drawable.home)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenAspect = maxWidth / maxHeight

        // The artwork always fills the screen with ContentScale.Crop, anchored
        // to the bottom edge so the farm scene, activity signs, and PROFILE
        // button are never cropped - only the decorative sky/title strip at
        // the very top gets trimmed on wide screens.
        val visLeft: Float
        val visRight: Float
        val visTop: Float
        val visBottom: Float
        if (screenAspect >= IMAGE_ASPECT) {
            visLeft = 0f
            visRight = 1f
            visTop = 1f - IMAGE_ASPECT / screenAspect
            visBottom = 1f
        } else {
            visTop = 0f
            visBottom = 1f
            val overflow = 1f - screenAspect / IMAGE_ASPECT
            visLeft = overflow / 2f
            visRight = 1f - overflow / 2f
        }

        fun mapX(f: Float) = (f - visLeft) / (visRight - visLeft)
        fun mapY(f: Float) = (f - visTop) / (visBottom - visTop)

        Image(
            painter = homeArt,
            contentDescription = "SGB-READ farm home screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter
        )

        fun hotspotModifier(spot: HotSpot, onClick: () -> Unit): Modifier = Modifier
            .offset(x = maxWidth * mapX(spot.left), y = maxHeight * mapY(spot.top))
            .size(maxWidth * (mapX(spot.right) - mapX(spot.left)), maxHeight * (mapY(spot.bottom) - mapY(spot.top)))
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
                            x = maxWidth * mapX(spot.right) - 18.dp,
                            y = maxHeight * mapY(spot.top) - 6.dp
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

        Box(modifier = hotspotModifier(PROFILE_SPOT, onProfileSelected))

        // The artwork's own speaker icon is cropped off the top of the
        // screen on wide aspect ratios, so it's redrawn here to keep the
        // "hear instructions" affordance available.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(SunYellow)
                .clickable { audio.speak(WELCOME_MESSAGE, rate = 0.9f) },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear instructions", tint = TextBrown)
        }
    }
}
