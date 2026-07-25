package com.sgbread.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.annotation.DrawableRes
import com.sgbread.app.R
import com.sgbread.app.ui.theme.AppDimens
import com.sgbread.app.ui.theme.LocalAppDimens
import com.sgbread.app.ui.theme.ProvideAppDimens
import com.sgbread.app.ui.theme.SgbReadTheme

/**
 * A picture paired with a title and body copy, laid out for whichever orientation it's
 * measured in: pinned image + scrolling text side-by-side in landscape (where height is
 * the scarce resource), stacked top-to-bottom in portrait. Reads sizing from
 * [LocalAppDimens], so wrap the call site in [ProvideAppDimens].
 */
@Composable
fun AdaptiveMediaContent(
    @DrawableRes image: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current

    if (dimens.isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = dimens.horizontalPadding, vertical = dimens.verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = title,
                modifier = Modifier.size(dimens.largePictureSize),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing / 2)
            ) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Text(body, style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.horizontalPadding, vertical = dimens.verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimens.spacing)
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = title,
                modifier = Modifier.size(dimens.pictureSize),
                contentScale = ContentScale.Fit
            )
            Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(body, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

private const val PREVIEW_BODY =
    "The duck says quack! Ducks live near the pond on the farm and love to swim in the cool water all day long."

@Preview(name = "Portrait", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun AdaptiveMediaContentPortraitPreview() {
    SgbReadTheme {
        ProvideAppDimens {
            AdaptiveMediaContent(image = R.drawable.duck, title = "Duck", body = PREVIEW_BODY)
        }
    }
}

@Preview(name = "Landscape (compact height)", widthDp = 740, heightDp = 360, showBackground = true)
@Composable
private fun AdaptiveMediaContentLandscapePreview() {
    SgbReadTheme {
        ProvideAppDimens {
            AdaptiveMediaContent(image = R.drawable.duck, title = "Duck", body = PREVIEW_BODY)
        }
    }
}
