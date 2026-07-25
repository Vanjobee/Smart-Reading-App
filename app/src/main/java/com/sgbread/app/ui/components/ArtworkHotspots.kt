package com.sgbread.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

data class ArtworkHotspot(
    val key: String,
    val contentDescription: String,
    val leftPx: Float,
    val topPx: Float,
    val rightPx: Float,
    val bottomPx: Float
)

/**
 * Places accessible tap targets over controls painted into a cropped source image.
 * The same crop transform used by ContentScale.Crop is reproduced here so targets
 * stay aligned on narrow and tall phones.
 */
@Composable
fun ArtworkHotspotOverlay(
    hotspots: List<ArtworkHotspot>,
    enabled: Boolean,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    sourceWidthPx: Float = 941f,
    sourceHeightPx: Float = 1672f
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val scale = maxOf(maxWidth.value / sourceWidthPx, maxHeight.value / sourceHeightPx)
        val imageLeft = (maxWidth.value - sourceWidthPx * scale) / 2f
        val imageTop = (maxHeight.value - sourceHeightPx * scale) / 2f

        hotspots.forEach { hotspot ->
            val left = imageLeft + hotspot.leftPx * scale
            val top = imageTop + hotspot.topPx * scale
            val width = (hotspot.rightPx - hotspot.leftPx) * scale
            val height = (hotspot.bottomPx - hotspot.topPx) * scale

            Box(
                modifier = Modifier
                    .offset(x = left.dp, y = top.dp)
                    .size(width = width.dp, height = height.dp)
                    .semantics { contentDescription = hotspot.contentDescription }
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                        onClickLabel = hotspot.contentDescription,
                        onClick = { onSelected(hotspot.key) }
                    )
            )
        }
    }
}
