package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bumblebee202111.minusonecloudmusic.R
@Composable
fun DragonBallRow(
    dragonBalls: List<MineDragonBall>,
    onItemClick: (MineDragonBall) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dragon_ball_spacing))
    ) {
        dragonBalls.forEach { dragonBall ->
            DragonBallItem(
                dragonBall = dragonBall,
                onClick = { onItemClick(dragonBall) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DragonBallItem(
    dragonBall: MineDragonBall,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x26FFFFFF))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = dragonBall.iconResId),
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .alpha(0.8f),
                contentScale = ContentScale.Inside
            )
            Text(
                text = stringResource(id = dragonBall.nameResId),
                modifier = Modifier.padding(start = 4.dp),
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}