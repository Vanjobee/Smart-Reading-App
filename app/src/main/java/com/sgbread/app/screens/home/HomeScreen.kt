package com.sgbread.app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.data.Modules
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.ui.components.FarmBackground
import com.sgbread.app.ui.components.FarmTile
import com.sgbread.app.ui.components.PlantProgressBar

@Composable
fun HomeScreen(
    progressViewModel: ProgressViewModel,
    onModuleSelected: (String) -> Unit
) {
    val progress by progressViewModel.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        FarmBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "SGB-READ",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Grow your reading on the farm!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            PlantProgressBar(fraction = progress.fraction, modifier = Modifier.padding(vertical = 16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().height(420.dp)
            ) {
                items(Modules.all) { module ->
                    FarmTile(
                        icon = module.icon,
                        title = "Module ${module.number}",
                        subtitle = module.title,
                        completed = progress.isModuleComplete(module.id),
                        onClick = { onModuleSelected(module.id) }
                    )
                }
            }
        }
    }
}
