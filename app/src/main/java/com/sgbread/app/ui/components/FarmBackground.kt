package com.sgbread.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.sgbread.app.ui.theme.NipaThatch
import com.sgbread.app.ui.theme.RiceGreen
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SkyBlueLight
import com.sgbread.app.ui.theme.SoilBrownDark
import com.sgbread.app.ui.theme.SunYellow
import com.sgbread.app.ui.theme.WaterBlue

/**
 * A gentle rice-field backdrop: sky, sun, terraced paddies with irrigation
 * canals, and a small nipa hut silhouette, drawn behind screen content.
 */
@Composable
fun FarmBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(SkyBlueLight, size = Size(w, h))
        drawCircle(SunYellow, radius = w * 0.09f, center = Offset(w * 0.84f, h * 0.1f))

        // Nipa hut silhouette on the horizon.
        val hutBaseY = h * 0.5f
        val roof = Path().apply {
            moveTo(w * 0.06f, hutBaseY)
            lineTo(w * 0.16f, hutBaseY - h * 0.06f)
            lineTo(w * 0.26f, hutBaseY)
            close()
        }
        drawPath(roof, NipaThatch)

        // Terraced rice paddies with irrigation canals.
        val rows = 5
        for (i in 0 until rows) {
            val top = hutBaseY + (h - hutBaseY) * (i / rows.toFloat())
            val bottom = hutBaseY + (h - hutBaseY) * ((i + 1) / rows.toFloat())
            val paddy = Path().apply {
                moveTo(0f, top)
                lineTo(w, top - h * 0.01f)
                lineTo(w, bottom)
                lineTo(0f, bottom + h * 0.01f)
                close()
            }
            drawPath(paddy, if (i % 2 == 0) RiceGreen else RiceGreenDark)
            drawRect(WaterBlue.copy(alpha = 0.35f), topLeft = Offset(0f, bottom - h * 0.01f), size = Size(w, h * 0.02f))
        }
        drawRect(SoilBrownDark.copy(alpha = 0.5f), topLeft = Offset(0f, h - h * 0.02f), size = Size(w, h * 0.02f))
    }
}
