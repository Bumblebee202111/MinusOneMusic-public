package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme

@Composable
fun Toolbar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.aid,
    iconTint: Color = Color(0xFF333333),
    titleColor: Color = DolphinTheme.colors.text1
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.toolbar_size)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = iconTint
            )
        }

        Text(
            text = title,
            color = titleColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun Toolbar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconRes: Int = R.drawable.aid,
    iconTint: Color = Color(0xFF333333),
    centerContent: @Composable () -> Unit,
    trailingContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.toolbar_size))
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Back",
                tint = iconTint
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            centerContent()
        }

        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            trailingContent()
        }
    }
}