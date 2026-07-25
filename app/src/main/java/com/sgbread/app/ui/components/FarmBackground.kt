package com.sgbread.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sgbread.app.R
import com.sgbread.app.ui.theme.CreamWhite

/**
 * Shared activity backdrop using the provided farm artwork.
 */
@Composable
fun FarmBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamWhite)
    ) {
        Image(
            painter = painterResource(R.drawable.background_portrait),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.62f),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CreamWhite.copy(alpha = 0.34f),
                            Color.White.copy(alpha = 0.18f),
                            CreamWhite.copy(alpha = 0.52f)
                        )
                    )
                )
        )
    }
}
