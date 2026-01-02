package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme



@Composable
fun DolphinToast(
    message: String,
    modifier: Modifier = Modifier
) {

    val toastShape: Shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .dropShadow(
                shape = toastShape,
                shadow = Shadow(

                    color = Color(0x1E000000),

                    offset = DpOffset(x = 0.dp, y = 2.dp),

                    radius = 6.dp
                )
            ).background(
                color = DolphinTheme.colors.backgroundDual,
                shape = toastShape
            ),
        color = DolphinTheme.colors.backgroundDual,
        shape = toastShape
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
                color = DolphinTheme.colors.textDual1,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Light Theme")
@Composable
fun DolphinToastPreviewLight() {
    DolphinTheme(darkTheme = false) {
        DolphinToast(message = "This is a toast message")
    }
}

@Preview(name = "Dark Theme")
@Composable
fun DolphinToastPreviewDark() {
    DolphinTheme(darkTheme = true) {
        DolphinToast(message = "This is a toast message")
    }
}