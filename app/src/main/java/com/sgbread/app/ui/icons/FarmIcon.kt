package com.sgbread.app.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.sgbread.app.data.FarmIconKey
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.SoilBrown
import com.sgbread.app.ui.theme.SoilBrownDark

/**
 * Simple flat, child-friendly farm illustrations drawn with vector shapes so the
 * app never needs bitmap art assets. Every icon is drawn inside a unit square
 * (0f..1f), scaled to whatever [modifier] size is supplied.
 */
@Composable
fun FarmIcon(key: FarmIconKey, modifier: Modifier = Modifier, background: Color? = CreamWhite) {
    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .then(if (background != null) Modifier.background(background, CircleShape) else Modifier)
    ) {
        draw(key)
    }
}

private fun DrawScope.pt(x: Float, y: Float) = Offset(x * size.minDimension, y * size.minDimension)
private fun DrawScope.len(v: Float) = v * size.minDimension

private fun DrawScope.oval(color: Color, cx: Float, cy: Float, rx: Float, ry: Float) {
    drawOval(color, topLeft = pt(cx - rx, cy - ry), size = Size(len(rx * 2), len(ry * 2)))
}

private fun DrawScope.circle(color: Color, cx: Float, cy: Float, r: Float) {
    drawCircle(color, radius = len(r), center = pt(cx, cy))
}

private fun DrawScope.roundRect(color: Color, x: Float, y: Float, w: Float, h: Float, corner: Float = 0.05f) {
    drawRoundRect(
        color, topLeft = pt(x, y), size = Size(len(w), len(h)),
        cornerRadius = CornerRadius(len(corner), len(corner))
    )
}

private fun DrawScope.tri(color: Color, a: Offset, b: Offset, c: Offset) {
    val p = Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close() }
    drawPath(p, color)
}

private fun DrawScope.trif(color: Color, ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float) =
    tri(color, pt(ax, ay), pt(bx, by), pt(cx, cy))

private fun DrawScope.line(color: Color, x1: Float, y1: Float, x2: Float, y2: Float, w: Float = 0.02f) {
    drawLine(color, pt(x1, y1), pt(x2, y2), strokeWidth = len(w), cap = StrokeCap.Round)
}

// ---- shared creature/produce builders ----

private fun DrawScope.mammal(body: Color, ear: Color, earUp: Boolean = true, snout: Color? = null) {
    oval(body, 0.5f, 0.62f, 0.32f, 0.24f) // body
    circle(body, 0.5f, 0.34f, 0.2f) // head
    if (earUp) {
        trif(ear, 0.3f, 0.24f, 0.36f, 0.06f, 0.42f, 0.22f)
        trif(ear, 0.7f, 0.24f, 0.64f, 0.06f, 0.58f, 0.22f)
    } else {
        oval(ear, 0.28f, 0.34f, 0.08f, 0.14f)
        oval(ear, 0.72f, 0.34f, 0.08f, 0.14f)
    }
    if (snout != null) oval(snout, 0.5f, 0.4f, 0.1f, 0.07f)
    circle(Color.Black, 0.42f, 0.32f, 0.02f)
    circle(Color.Black, 0.58f, 0.32f, 0.02f)
    // legs
    for (dx in listOf(-0.2f, -0.07f, 0.07f, 0.2f)) roundRect(body, 0.5f + dx - 0.03f, 0.78f, 0.06f, 0.14f)
}

private fun DrawScope.bird(body: Color, comb: Color = Color(0xFFE0563B)) {
    oval(body, 0.5f, 0.58f, 0.26f, 0.22f)
    circle(body, 0.52f, 0.32f, 0.15f)
    trif(comb, 0.44f, 0.2f, 0.48f, 0.08f, 0.52f, 0.2f)
    trif(comb, 0.5f, 0.2f, 0.55f, 0.06f, 0.58f, 0.2f)
    trif(Color(0xFFFFC94A), 0.62f, 0.34f, 0.76f, 0.32f, 0.62f, 0.4f) // beak
    circle(Color.Black, 0.48f, 0.3f, 0.018f)
    trif(Color(0xFFFFC94A), 0.44f, 0.78f, 0.4f, 0.9f, 0.48f, 0.78f)
    trif(Color(0xFFFFC94A), 0.56f, 0.78f, 0.6f, 0.9f, 0.52f, 0.78f)
}

private fun DrawScope.leafyFruit(fruitColor: Color, shapeWide: Boolean = false) {
    if (shapeWide) oval(fruitColor, 0.5f, 0.56f, 0.28f, 0.22f) else oval(fruitColor, 0.5f, 0.55f, 0.22f, 0.28f)
    trif(Color(0xFF6FB53F), 0.5f, 0.3f, 0.4f, 0.16f, 0.6f, 0.18f)
    line(SoilBrownDark, 0.5f, 0.3f, 0.5f, 0.2f, 0.02f)
}

private fun DrawScope.jar(liquid: Color) {
    roundRect(CreamWhite, 0.32f, 0.3f, 0.36f, 0.5f, 0.08f)
    roundRect(liquid, 0.34f, 0.42f, 0.32f, 0.36f, 0.06f)
    roundRect(SoilBrown, 0.36f, 0.2f, 0.28f, 0.12f, 0.04f)
}

// ---- dispatcher ----

private fun DrawScope.draw(key: FarmIconKey) {
    val skin = Color(0xFFF2A65A)
    when (key) {
        FarmIconKey.ANT -> {
            circle(Color(0xFF3B2A1E), 0.5f, 0.3f, 0.08f)
            circle(Color(0xFF3B2A1E), 0.5f, 0.48f, 0.12f)
            oval(Color(0xFF3B2A1E), 0.5f, 0.72f, 0.15f, 0.18f)
            for (dx in listOf(-0.18f, 0f, 0.18f)) line(Color(0xFF3B2A1E), 0.5f, 0.55f, 0.5f + dx, 0.7f, 0.015f)
            line(Color(0xFF3B2A1E), 0.44f, 0.24f, 0.38f, 0.14f, 0.015f)
            line(Color(0xFF3B2A1E), 0.56f, 0.24f, 0.62f, 0.14f, 0.015f)
        }
        FarmIconKey.BEE -> {
            oval(Color(0xFFFFC94A), 0.5f, 0.55f, 0.22f, 0.18f)
            line(Color(0xFF3B2A1E), 0.34f, 0.45f, 0.34f, 0.65f, 0.03f)
            line(Color(0xFF3B2A1E), 0.5f, 0.4f, 0.5f, 0.7f, 0.03f)
            line(Color(0xFF3B2A1E), 0.66f, 0.45f, 0.66f, 0.65f, 0.03f)
            circle(Color(0xFF3B2A1E), 0.72f, 0.55f, 0.07f)
            oval(Color(0xB3FFFFFF), 0.42f, 0.35f, 0.14f, 0.09f)
            oval(Color(0xB3FFFFFF), 0.58f, 0.35f, 0.14f, 0.09f)
        }
        FarmIconKey.CAT -> mammal(Color(0xFFB0876A), Color(0xFFB0876A), earUp = true, snout = CreamWhite)
        FarmIconKey.COW -> {
            mammal(CreamWhite, Color(0xFF6B4623), earUp = false, snout = Color(0xFFF6C9C9))
            oval(Color(0xFF3B2A1E), 0.4f, 0.62f, 0.05f, 0.06f)
            oval(Color(0xFF3B2A1E), 0.6f, 0.68f, 0.06f, 0.05f)
        }
        FarmIconKey.DOG -> mammal(Color(0xFFD9A441), Color(0xFF8B5E34), earUp = false, snout = CreamWhite)
        FarmIconKey.DUCK -> {
            oval(Color(0xFFFFF6D9), 0.5f, 0.6f, 0.26f, 0.2f)
            circle(Color(0xFFFFF6D9), 0.62f, 0.36f, 0.14f)
            trif(Color(0xFFFFC94A), 0.72f, 0.36f, 0.85f, 0.33f, 0.72f, 0.42f)
            circle(Color.Black, 0.66f, 0.33f, 0.016f)
        }
        FarmIconKey.FROG -> {
            oval(Color(0xFF6FB53F), 0.5f, 0.6f, 0.26f, 0.2f)
            circle(Color(0xFF6FB53F), 0.36f, 0.4f, 0.1f)
            circle(Color(0xFF6FB53F), 0.64f, 0.4f, 0.1f)
            circle(Color.Black, 0.36f, 0.4f, 0.035f)
            circle(Color.Black, 0.64f, 0.4f, 0.035f)
            oval(Color(0xFF4F8A3E), 0.5f, 0.55f, 0.16f, 0.06f)
        }
        FarmIconKey.GOAT -> mammal(Color(0xFFEDE3D0), Color(0xFF8B5E34), earUp = false, snout = Color(0xFFD9C9A8))
        FarmIconKey.HEN -> bird(Color(0xFFEDE3D0))
        FarmIconKey.CHICK, FarmIconKey.CHICKEN -> bird(Color(0xFFFFC94A))
        FarmIconKey.PIG -> mammal(Color(0xFFF2A6C2), Color(0xFFE0819F), earUp = true, snout = Color(0xFFE0819F))
        FarmIconKey.RAT -> {
            mammal(Color(0xFF9E9E9E), Color(0xFFF6C9C9), earUp = false)
            line(Color(0xFF9E9E9E), 0.5f, 0.78f, 0.7f, 0.9f, 0.02f)
        }
        FarmIconKey.SHEEP -> {
            oval(CreamWhite, 0.5f, 0.6f, 0.3f, 0.22f)
            circle(CreamWhite, 0.4f, 0.6f, 0.12f)
            circle(CreamWhite, 0.55f, 0.55f, 0.12f)
            circle(CreamWhite, 0.65f, 0.65f, 0.12f)
            circle(Color(0xFF4A3423), 0.5f, 0.34f, 0.14f)
            circle(Color.Black, 0.46f, 0.32f, 0.018f)
            circle(Color.Black, 0.54f, 0.32f, 0.018f)
        }
        FarmIconKey.EGG -> {
            oval(CreamWhite, 0.5f, 0.55f, 0.2f, 0.28f)
            oval(Color(0xFFEFE0C0), 0.5f, 0.55f, 0.2f, 0.28f)
        }
        FarmIconKey.MILK -> {
            val p = Path().apply {
                moveTo(len(0.38f), len(0.2f)); lineTo(len(0.62f), len(0.2f))
                lineTo(len(0.7f), len(0.85f)); lineTo(len(0.3f), len(0.85f)); close()
            }
            drawPath(p, CreamWhite)
            roundRect(Color(0xFF4FA8D8), 0.33f, 0.5f, 0.34f, 0.32f, 0.02f)
            roundRect(SoilBrown, 0.4f, 0.16f, 0.2f, 0.06f, 0.02f)
        }
        FarmIconKey.SUN -> {
            circle(Color(0xFFFFC94A), 0.5f, 0.5f, 0.22f)
            for (i in 0 until 8) {
                val a = (i * 45.0) * Math.PI / 180.0
                val x1 = 0.5f + 0.28f * kotlin.math.cos(a).toFloat(); val y1 = 0.5f + 0.28f * kotlin.math.sin(a).toFloat()
                val x2 = 0.5f + 0.4f * kotlin.math.cos(a).toFloat(); val y2 = 0.5f + 0.4f * kotlin.math.sin(a).toFloat()
                line(Color(0xFFFFC94A), x1, y1, x2, y2, 0.035f)
            }
        }
        FarmIconKey.RAIN -> {
            oval(Color(0xFFE8F0F5), 0.5f, 0.35f, 0.26f, 0.16f)
            oval(Color(0xFFE8F0F5), 0.34f, 0.4f, 0.16f, 0.12f)
            oval(Color(0xFFE8F0F5), 0.66f, 0.4f, 0.16f, 0.12f)
            for (dx in listOf(-0.18f, 0f, 0.18f)) line(Color(0xFF4FA8D8), 0.5f + dx, 0.58f, 0.5f + dx - 0.05f, 0.82f, 0.03f)
        }
        FarmIconKey.WIND -> {
            for (i in 0 until 3) {
                val yy = 0.35f + i * 0.18f
                val p = Path().apply {
                    moveTo(len(0.2f), len(yy))
                    quadraticTo(len(0.5f), len(yy - 0.08f), len(0.8f), len(yy))
                }
                drawPath(p, Color(0xFF8ECFEE), style = Stroke(width = len(0.03f), cap = StrokeCap.Round))
            }
        }
        FarmIconKey.MUD, FarmIconKey.SOIL, FarmIconKey.WET -> {
            oval(SoilBrown, 0.5f, 0.6f, 0.34f, 0.2f)
            oval(SoilBrownDark, 0.4f, 0.6f, 0.08f, 0.04f)
            oval(SoilBrownDark, 0.6f, 0.65f, 0.06f, 0.03f)
            if (key == FarmIconKey.WET) {
                oval(Color(0xFF4FA8D8), 0.5f, 0.28f, 0.09f, 0.12f)
            }
        }
        FarmIconKey.HEAT -> {
            roundRect(CreamWhite, 0.44f, 0.15f, 0.12f, 0.5f, 0.06f)
            circle(Color(0xFFE57373), 0.5f, 0.68f, 0.12f)
            roundRect(Color(0xFFE57373), 0.47f, 0.25f, 0.06f, 0.42f, 0.03f)
        }
        FarmIconKey.WEED -> {
            for (dx in listOf(-0.12f, 0f, 0.12f)) {
                val p = Path().apply {
                    moveTo(len(0.5f + dx), len(0.85f))
                    quadraticTo(len(0.5f + dx * 2), len(0.5f), len(0.5f + dx), len(0.2f))
                }
                drawPath(p, Color(0xFF6FB53F), style = Stroke(width = len(0.03f), cap = StrokeCap.Round))
            }
        }
        FarmIconKey.STRAW, FarmIconKey.GRAIN -> {
            oval(Color(0xFFE0B04A), 0.5f, 0.72f, 0.22f, 0.12f)
            for (dx in listOf(-0.1f, 0f, 0.1f)) line(Color(0xFFC99A34), 0.5f + dx, 0.72f, 0.5f + dx, 0.22f, 0.025f)
        }
        FarmIconKey.SEED, FarmIconKey.SEEDS -> {
            oval(Color(0xFFC99A34), 0.4f, 0.5f, 0.12f, 0.16f)
            if (key == FarmIconKey.SEEDS) oval(Color(0xFFC99A34), 0.62f, 0.6f, 0.11f, 0.14f)
        }
        FarmIconKey.BARN -> {
            roundRect(Color(0xFFE0563B), 0.22f, 0.45f, 0.56f, 0.4f, 0.02f)
            trif(Color(0xFF9C3D2A), 0.15f, 0.45f, 0.5f, 0.18f, 0.85f, 0.45f)
            roundRect(CreamWhite, 0.44f, 0.65f, 0.12f, 0.2f, 0.02f)
            circle(Color(0xFFFFF6D9), 0.5f, 0.34f, 0.06f)
        }
        FarmIconKey.SHED -> {
            roundRect(Color(0xFFD9A441), 0.26f, 0.4f, 0.48f, 0.45f, 0.02f)
            trif(Color(0xFF8B5E34), 0.22f, 0.4f, 0.5f, 0.2f, 0.78f, 0.4f)
            roundRect(SoilBrownDark, 0.44f, 0.65f, 0.12f, 0.2f, 0.01f)
        }
        FarmIconKey.NIPA_HUT -> {
            trif(Color(0xFFD9A441), 0.14f, 0.5f, 0.5f, 0.16f, 0.86f, 0.5f)
            roundRect(Color(0xFFC99A34), 0.26f, 0.5f, 0.48f, 0.32f, 0.01f)
            for (dx in listOf(-0.16f, 0f, 0.16f)) roundRect(SoilBrown, 0.5f + dx - 0.015f, 0.82f, 0.03f, 0.15f)
        }
        FarmIconKey.SACK -> {
            val p = Path().apply {
                moveTo(len(0.32f), len(0.3f)); lineTo(len(0.68f), len(0.3f))
                lineTo(len(0.78f), len(0.85f)); lineTo(len(0.22f), len(0.85f)); close()
            }
            drawPath(p, Color(0xFFD9A441))
            roundRect(SoilBrown, 0.35f, 0.2f, 0.3f, 0.12f, 0.05f)
        }
        FarmIconKey.PAIL -> {
            val p = Path().apply {
                moveTo(len(0.3f), len(0.4f)); lineTo(len(0.7f), len(0.4f))
                lineTo(len(0.64f), len(0.85f)); lineTo(len(0.36f), len(0.85f)); close()
            }
            drawPath(p, Color(0xFF9E9E9E))
            drawArc(Color(0xFF6B4623), 200f, 140f, false, topLeft = pt(0.3f, 0.15f), size = Size(len(0.4f), len(0.3f)), style = Stroke(width = len(0.025f)))
        }
        FarmIconKey.ROPE -> {
            val p = Path().apply {
                moveTo(len(0.2f), len(0.8f))
                for (i in 1..6) {
                    val x = 0.2f + i * 0.1f; val y = if (i % 2 == 0) 0.8f else 0.5f
                    lineTo(len(x), len(y))
                }
            }
            drawPath(p, Color(0xFFD9A441), style = Stroke(width = len(0.05f), cap = StrokeCap.Round))
        }
        FarmIconKey.SHOVEL, FarmIconKey.SPADE -> {
            line(SoilBrown, 0.4f, 0.85f, 0.62f, 0.2f, 0.045f)
            trif(Color(0xFF9E9E9E), 0.62f, 0.2f, 0.78f, 0.28f, 0.66f, 0.4f)
        }
        FarmIconKey.LADDER -> {
            line(SoilBrown, 0.32f, 0.15f, 0.32f, 0.85f, 0.035f)
            line(SoilBrown, 0.68f, 0.15f, 0.68f, 0.85f, 0.035f)
            for (yy in listOf(0.3f, 0.5f, 0.7f)) line(Color(0xFFD9A441), 0.32f, yy, 0.68f, yy, 0.035f)
        }
        FarmIconKey.BASKET -> {
            val p = Path().apply {
                moveTo(len(0.24f), len(0.42f)); lineTo(len(0.76f), len(0.42f))
                lineTo(len(0.68f), len(0.85f)); lineTo(len(0.32f), len(0.85f)); close()
            }
            drawPath(p, Color(0xFFD9A441))
            for (yy in listOf(0.5f, 0.62f, 0.74f)) line(Color(0xFFB0791F), 0.28f, yy, 0.72f, yy, 0.015f)
            drawArc(Color(0xFF8B5E34), 190f, 160f, false, topLeft = pt(0.28f, 0.14f), size = Size(len(0.44f), len(0.34f)), style = Stroke(width = len(0.03f)))
        }
        FarmIconKey.CARABAO -> {
            mammal(Color(0xFF5B6A69), Color(0xFF3F4A49), earUp = false, snout = Color(0xFF3F4A49))
            val p = Path().apply {
                moveTo(len(0.3f), len(0.24f))
                quadraticTo(len(0.5f), len(0.02f), len(0.7f), len(0.24f))
            }
            drawPath(p, Color(0xFFE0E0E0), style = Stroke(width = len(0.035f)))
        }
        FarmIconKey.CORN -> {
            oval(Color(0xFFFFD866), 0.5f, 0.55f, 0.16f, 0.3f)
            for (yy in 0..5) for (xx in listOf(-0.08f, 0f, 0.08f)) circle(Color(0xFFE0B04A), 0.5f + xx, 0.32f + yy * 0.08f, 0.025f)
            trif(Color(0xFF6FB53F), 0.5f, 0.28f, 0.36f, 0.14f, 0.42f, 0.3f)
            trif(Color(0xFF6FB53F), 0.5f, 0.28f, 0.64f, 0.14f, 0.58f, 0.3f)
        }
        FarmIconKey.RICE -> {
            oval(CreamWhite, 0.5f, 0.65f, 0.26f, 0.14f)
            for (dx in listOf(-0.14f, 0f, 0.14f)) oval(CreamWhite, 0.5f + dx, 0.55f, 0.06f, 0.04f)
        }
        FarmIconKey.OKRA -> {
            val p = Path().apply {
                moveTo(len(0.5f), len(0.15f))
                lineTo(len(0.58f), len(0.6f))
                lineTo(len(0.5f), len(0.85f))
                lineTo(len(0.42f), len(0.6f))
                close()
            }
            drawPath(p, Color(0xFF6FB53F))
        }
        FarmIconKey.BEAN -> {
            val p = Path().apply {
                moveTo(len(0.32f), len(0.3f))
                quadraticTo(len(0.2f), len(0.5f), len(0.35f), len(0.7f))
                quadraticTo(len(0.55f), len(0.9f), len(0.68f), len(0.7f))
                quadraticTo(len(0.8f), len(0.5f), len(0.65f), len(0.3f))
                quadraticTo(len(0.5f), len(0.15f), len(0.32f), len(0.3f))
                close()
            }
            drawPath(p, Color(0xFF6FB53F))
        }
        FarmIconKey.BANANA -> {
            val p = Path().apply {
                moveTo(len(0.28f), len(0.72f))
                quadraticTo(len(0.3f), len(0.25f), len(0.7f), len(0.22f))
                quadraticTo(len(0.62f), len(0.35f), len(0.5f), len(0.5f))
                quadraticTo(len(0.4f), len(0.65f), len(0.4f), len(0.78f))
                close()
            }
            drawPath(p, Color(0xFFFFD866))
        }
        FarmIconKey.MANGO -> leafyFruit(Color(0xFFF2A65A))
        FarmIconKey.ONION -> {
            oval(Color(0xFFE0A9D0), 0.5f, 0.58f, 0.22f, 0.24f)
            line(Color(0xFF6FB53F), 0.5f, 0.34f, 0.5f, 0.14f, 0.025f)
        }
        FarmIconKey.TOMATO -> leafyFruit(Color(0xFFE0563B), shapeWide = true)
        FarmIconKey.GARLIC -> {
            oval(CreamWhite, 0.5f, 0.6f, 0.2f, 0.22f)
            line(Color(0xFFD9C9A8), 0.5f, 0.42f, 0.5f, 0.2f, 0.02f)
        }
        FarmIconKey.COFFEE -> {
            circle(Color(0xFF6B4623), 0.42f, 0.5f, 0.14f)
            circle(Color(0xFF6B4623), 0.6f, 0.58f, 0.14f)
            line(Color(0xFF4A3423), 0.42f, 0.4f, 0.42f, 0.6f, 0.015f)
            line(Color(0xFF4A3423), 0.6f, 0.48f, 0.6f, 0.68f, 0.015f)
        }
        FarmIconKey.HONEY -> jar(Color(0xFFE0A526))
        FarmIconKey.LEMON -> oval(Color(0xFFFFE066), 0.5f, 0.55f, 0.24f, 0.2f)
        FarmIconKey.MELON -> {
            circle(Color(0xFF6FB53F), 0.5f, 0.55f, 0.26f)
            for (dx in listOf(-0.14f, 0f, 0.14f)) line(Color(0xFF4F8A3E), 0.5f + dx, 0.3f, 0.5f + dx, 0.8f, 0.015f)
        }
        FarmIconKey.PAPAYA -> leafyFruit(Color(0xFFFFA24A))
        FarmIconKey.PEANUT -> {
            oval(Color(0xFFD9A441), 0.42f, 0.42f, 0.14f, 0.18f)
            oval(Color(0xFFD9A441), 0.58f, 0.62f, 0.14f, 0.18f)
        }
        FarmIconKey.RADISH -> {
            oval(Color(0xFFE0563B), 0.5f, 0.55f, 0.16f, 0.24f)
            trif(Color(0xFF6FB53F), 0.5f, 0.32f, 0.4f, 0.16f, 0.6f, 0.16f)
        }
        FarmIconKey.SQUASH -> {
            oval(Color(0xFFFFA24A), 0.5f, 0.58f, 0.28f, 0.2f)
            for (dx in listOf(-0.1f, 0.1f)) line(Color(0xFFE0863A), 0.5f + dx, 0.4f, 0.5f + dx, 0.76f, 0.02f)
        }
        FarmIconKey.GARDEN_PLANT -> {
            line(SoilBrownDark, 0.5f, 0.85f, 0.5f, 0.4f, 0.025f)
            oval(Color(0xFF6FB53F), 0.38f, 0.4f, 0.12f, 0.08f)
            oval(Color(0xFF6FB53F), 0.62f, 0.5f, 0.12f, 0.08f)
            oval(Color(0xFF8FCB5C), 0.5f, 0.3f, 0.1f, 0.08f)
        }
        FarmIconKey.SPROUT_SEED -> {
            oval(SoilBrown, 0.5f, 0.72f, 0.3f, 0.14f)
            oval(Color(0xFF6B4623), 0.5f, 0.6f, 0.08f, 0.1f)
        }
        FarmIconKey.SPROUT_SPROUT -> {
            oval(SoilBrown, 0.5f, 0.78f, 0.3f, 0.12f)
            line(Color(0xFF6FB53F), 0.5f, 0.7f, 0.5f, 0.45f, 0.03f)
            trif(Color(0xFF8FCB5C), 0.5f, 0.45f, 0.38f, 0.6f, 0.46f, 0.62f)
            trif(Color(0xFF8FCB5C), 0.5f, 0.45f, 0.62f, 0.6f, 0.54f, 0.62f)
        }
        FarmIconKey.SPROUT_GROWING -> {
            oval(SoilBrown, 0.5f, 0.82f, 0.32f, 0.12f)
            line(Color(0xFF4F8A3E), 0.5f, 0.75f, 0.5f, 0.25f, 0.035f)
            for (dy in listOf(0.35f, 0.5f, 0.63f)) {
                trif(Color(0xFF6FB53F), 0.5f, dy, 0.32f, dy - 0.1f, 0.4f, dy + 0.08f)
                trif(Color(0xFF6FB53F), 0.5f, dy, 0.68f, dy - 0.1f, 0.6f, dy + 0.08f)
            }
        }
        FarmIconKey.SPROUT_FLOWERING -> {
            oval(SoilBrown, 0.5f, 0.85f, 0.32f, 0.1f)
            line(Color(0xFF4F8A3E), 0.5f, 0.8f, 0.5f, 0.32f, 0.035f)
            for (dy in listOf(0.45f, 0.62f)) {
                trif(Color(0xFF6FB53F), 0.5f, dy, 0.3f, dy - 0.08f, 0.38f, dy + 0.1f)
                trif(Color(0xFF6FB53F), 0.5f, dy, 0.7f, dy - 0.08f, 0.62f, dy + 0.1f)
            }
            for (i in 0 until 6) {
                val a = (i * 60.0) * Math.PI / 180.0
                oval(Color(0xFFFFC94A), 0.5f + 0.09f * kotlin.math.cos(a).toFloat(), 0.22f + 0.09f * kotlin.math.sin(a).toFloat(), 0.06f, 0.06f)
            }
            circle(Color(0xFFE0563B), 0.5f, 0.22f, 0.06f)
        }
        FarmIconKey.SPROUT_HARVEST -> {
            oval(SoilBrown, 0.5f, 0.85f, 0.34f, 0.1f)
            circle(Color(0xFFE0563B), 0.36f, 0.55f, 0.14f)
            circle(Color(0xFFFFC94A), 0.58f, 0.42f, 0.13f)
            circle(Color(0xFF6FB53F), 0.62f, 0.65f, 0.12f)
            trif(Color(0xFF4F8A3E), 0.36f, 0.4f, 0.3f, 0.28f, 0.4f, 0.3f)
        }
    }
}
