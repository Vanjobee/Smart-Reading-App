package com.sgbread.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.data.FarmIconKey
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SunYellow

enum class GrowthStage(val label: String, val icon: FarmIconKey) {
    SEED("Seed", FarmIconKey.SPROUT_SEED),
    SPROUT("Sprout", FarmIconKey.SPROUT_SPROUT),
    GROWING("Growing Plant", FarmIconKey.SPROUT_GROWING),
    FLOWERING("Flowering Plant", FarmIconKey.SPROUT_FLOWERING),
    HARVEST("Harvest", FarmIconKey.SPROUT_HARVEST);

    companion object {
        /** Maps how much of the app a child has finished (0f..1f) to a growth stage. */
        fun fromProgress(fraction: Float): GrowthStage = when {
            fraction >= 0.999f -> HARVEST
            fraction >= 0.75f -> FLOWERING
            fraction >= 0.5f -> GROWING
            fraction > 0f -> SPROUT
            else -> SEED
        }
    }
}

@Composable
fun PlantProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val stage = GrowthStage.fromProgress(fraction)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FarmIcon(stage.icon, modifier = Modifier.size(40.dp), background = null)
            Text(stage.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(50))
                .size(width = 220.dp, height = 10.dp),
            color = RiceGreenDark,
            trackColor = SunYellow.copy(alpha = 0.3f)
        )
    }
}
