package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun DolphinToast(
    message: String,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF1D1D20) else Color.White
    val textColor = if (isDarkTheme) {
        Color(0xF2FFFFFF)
    } else {
        Color(0xFF0F0F24)
    }
    val shadowColor = Color(0x1E000000)

    Surface(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = shadowColor
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                modifier = Modifier.widthIn(max = 285.dp),
                color = textColor,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Preview
fun CustomToastPreview() {
    DolphinToast(message = "This is a toast message")
}