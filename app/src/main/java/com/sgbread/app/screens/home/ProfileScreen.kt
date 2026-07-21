package com.sgbread.app.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.data.Modules
import com.sgbread.app.progress.ProgressViewModel
import androidx.compose.foundation.layout.size
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.PlantProgressBar
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SunYellow

@Composable
fun ProfileScreen(
    progressViewModel: ProgressViewModel,
    onBack: () -> Unit
) {
    val progress by progressViewModel.state.collectAsStateWithLifecycle()

    ActivityScaffold(title = "Profile", onBack = onBack, onReplayInstructions = null) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PlantProgressBar(fraction = progress.fraction, modifier = Modifier.padding(bottom = 8.dp))
            Text(
                "${progress.completedActivityIds.size} of ${progress.totalActivities} activities complete",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Modules.all.forEach { module ->
                val fraction = progress.moduleFraction(module.id)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FarmIcon(module.icon, modifier = Modifier.size(48.dp).padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(module.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .height(8.dp),
                            color = if (fraction >= 1f) CorrectGreen else RiceGreenDark,
                            trackColor = SunYellow.copy(alpha = 0.3f)
                        )
                    }
                    Text(
                        "${module.activities.count { it.id in progress.completedActivityIds }}/${module.activities.size}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}
