package com.sgbread.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.data.FarmIconKey
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark

/** Full-screen "you finished the activity" celebration, shown once all questions are answered. */
@Composable
fun ActivityCompleteOverlay(onContinue: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 360.dp)
                .shadow(16.dp, RoundedCornerShape(32.dp))
                .background(CreamWhite, RoundedCornerShape(28.dp))
                .border(3.dp, RiceGreenDark.copy(alpha = 0.55f), RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FarmIcon(FarmIconKey.SPROUT_HARVEST, modifier = Modifier.size(96.dp), background = null)
            AutoSizeText(
                Praise.randomCorrect(),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
            Text(
                "You finished the activity!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )
            Button(onClick = onContinue, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = RiceGreenDark)) {
                Text("Continue", color = CreamWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}
