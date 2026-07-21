package com.sgbread.app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.data.ModuleInfo
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.FarmTile

@Composable
fun ModuleScreen(
    module: ModuleInfo,
    progressViewModel: ProgressViewModel,
    onActivitySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val progress by progressViewModel.state.collectAsStateWithLifecycle()

    ActivityScaffold(title = module.title, onBack = onBack, onReplayInstructions = null) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                module.activities.forEach { activity ->
                    FarmTile(
                        icon = module.icon,
                        title = activity.title,
                        subtitle = activity.description,
                        completed = activity.id in progress.completedActivityIds,
                        onClick = { onActivitySelected(activity.route) },
                        modifier = Modifier.weight(1f).fillMaxHeight()
                    )
                }
            }
        }
    }
}
