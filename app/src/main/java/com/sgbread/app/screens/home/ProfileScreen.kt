package com.sgbread.app.screens.home

import android.annotation.SuppressLint
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.ModuleInfo
import com.sgbread.app.data.Modules
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.PlantProgressBar
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.IncorrectRed
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SunYellow

private const val MIN_BAR_FRACTION = 0.05f

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProfileScreen(
    progressViewModel: ProgressViewModel,
    audio: AudioManager,
    onBack: () -> Unit
) {
    val progress by progressViewModel.state.collectAsStateWithLifecycle()
    var showResetConfirm by remember { mutableStateOf(false) }
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    fun playTapAndRun(action: () -> Unit) {
        audio.stopPlayback()
        audio.playSfx(Sfx.TAP)
        action()
    }

    // The app is landscape-locked, so the sidebar (summary + reset) sits beside the
    // chart rather than above it -- stacking them would starve the bars of height.
    ActivityScaffold(
        title = "Profile",
        onBack = { playTapAndRun(onBack) },
        onReplayInstructions = null,
        titleTextScale = responsiveTextScale,
        confirmOnBack = false
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            val compactProfile = maxWidth < 620.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )

            if (compactProfile) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(metrics.spacing)
                ) {
                    PlantProgressBar(
                        fraction = progress.fraction,
                        modifier = Modifier.fillMaxWidth(),
                        labelColor = CreamWhite,
                        labelTextScale = responsiveTextScale
                    )
                    Text(
                        "${progress.completedActivityIds.size} of ${progress.totalActivities} activities complete",
                        style = MaterialTheme.typography.bodyMedium.let { baseStyle ->
                            baseStyle.copy(fontSize = baseStyle.fontSize * responsiveTextScale)
                        },
                        color = CreamWhite,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Modules.all.forEach { module ->
                            ModuleBar(
                                module = module,
                                done = module.activities.count { it.id in progress.completedActivityIds },
                                fraction = progress.moduleFraction(module.id),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { playTapAndRun { showResetConfirm = true } },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CreamWhite),
                        border = BorderStroke(1.5.dp, CreamWhite.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Reset All Progress",
                            style = MaterialTheme.typography.labelLarge.let { baseStyle ->
                                baseStyle.copy(fontSize = baseStyle.fontSize * responsiveTextScale,
                                               color = CreamWhite)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.32f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PlantProgressBar(
                                fraction = progress.fraction,
                                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
                                labelColor = CreamWhite,
                                labelTextScale = responsiveTextScale
                            )
                            Text(
                                "${progress.completedActivityIds.size} of ${progress.totalActivities} activities complete",
                                style = MaterialTheme.typography.bodyMedium.let { baseStyle ->
                                    baseStyle.copy(fontSize = baseStyle.fontSize * responsiveTextScale)
                                },
                                color = CreamWhite,
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedButton(
                            onClick = { playTapAndRun { showResetConfirm = true } },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CreamWhite),
                            border = BorderStroke(1.5.dp, CreamWhite.copy(alpha = 0.85f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Reset All Progress",
                                style = MaterialTheme.typography.labelLarge.let { baseStyle ->
                                    baseStyle.copy(fontSize = baseStyle.fontSize * responsiveTextScale)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(0.68f)
                            .fillMaxHeight()
                            .padding(start = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Modules.all.forEach { module ->
                            ModuleBar(
                                module = module,
                                done = module.activities.count { it.id in progress.completedActivityIds },
                                fraction = progress.moduleFraction(module.id),
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Reset all progress?") },
            text = { Text("Every completed activity will be cleared and the farm will start growing again from a seed. This can't be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        playTapAndRun {
                            progressViewModel.resetProgress()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IncorrectRed)
                ) {
                    Text("Reset", color = CreamWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { playTapAndRun { } }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** One vertical bar in the module-progress chart: a value label, a bottom-anchored
 * fill bar sized to the module's completion fraction, then the module's icon and name. */
@Composable
private fun ModuleBar(module: ModuleInfo, done: Int, fraction: Float, modifier: Modifier = Modifier) {
    val barColor = if (fraction >= 1f) CorrectGreen else RiceGreenDark

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "$done/${module.activities.size}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = CreamWhite
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SunYellow.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(fraction.coerceIn(MIN_BAR_FRACTION, 1f))
                    .background(
                        barColor,
                        RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    )
            )
        }
        FarmIcon(module.icon, modifier = Modifier.size(30.dp).padding(top = 6.dp))
        Text(
            module.title,
            style = MaterialTheme.typography.labelLarge,
            color = CreamWhite,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
