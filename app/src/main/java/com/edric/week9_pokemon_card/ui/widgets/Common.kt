package com.edric.week9_pokemon_card.ui.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.edric.week9_pokemon_card.ui.theme.PokeCardBorder
import com.edric.week9_pokemon_card.ui.theme.PokeCardSurface
import com.edric.week9_pokemon_card.ui.theme.PokeErrorCircle
import com.edric.week9_pokemon_card.ui.theme.PokeErrorRed
import com.edric.week9_pokemon_card.ui.theme.PokeGreen
import com.edric.week9_pokemon_card.ui.theme.PokeGreenBorder
import com.edric.week9_pokemon_card.ui.theme.PokeGreenSurface
import com.edric.week9_pokemon_card.ui.theme.PokeRed
import com.edric.week9_pokemon_card.ui.theme.PokeTextPrimary
import com.edric.week9_pokemon_card.ui.theme.PokeTextSecondary
import java.util.Locale

fun formatUsd(price: Double?): String =
    if (price != null) String.format(Locale.US, "$%.2f", price) else "N/A"

@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    if (url.isNullOrBlank()) {
        Box(
            modifier = modifier.background(PokeCardSurface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.CatchingPokemon,
                contentDescription = contentDescription,
                tint = PokeTextSecondary,
                modifier = Modifier.size(36.dp),
            )
        }
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier,
        )
    }
}

@Composable
fun PokeballBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(PokeRed),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val strokeWidth = 2.dp.toPx()
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = Color.White,
                radius = radius - strokeWidth / 2f,
                center = center,
                style = Stroke(width = strokeWidth),
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, center.y),
                end = Offset(center.x - radius * 0.34f, center.y),
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x + radius * 0.34f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = strokeWidth,
            )
            drawCircle(
                color = Color.White,
                radius = radius * 0.32f,
                center = center,
                style = Stroke(width = strokeWidth),
            )
        }
    }
}

@Composable
fun LiveApiBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = PokeGreenSurface,
        border = BorderStroke(1.dp, PokeGreenBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(PokeGreen),
            )
            Text(
                text = "LIVE API",
                color = PokeGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = PokeRed)
    }
}

@Composable
fun ConnectionErrorBox(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(PokeErrorCircle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.WifiOff,
                contentDescription = "Connection Failed",
                tint = PokeErrorRed,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Connection Failed",
            color = PokeErrorRed,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            color = PokeTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PokeRed,
                contentColor = PokeTextPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Try Again",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
fun EmptyResultsBox(
    query: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No cards found",
            color = PokeTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "No results matched \"$query\". Try another Pokemon name.",
            color = PokeTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}
