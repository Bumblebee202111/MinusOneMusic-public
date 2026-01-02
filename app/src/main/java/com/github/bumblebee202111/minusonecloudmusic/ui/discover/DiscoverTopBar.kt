package com.github.bumblebee202111.minusonecloudmusic.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        DiscoverMenuIcon(onMenuClick)
        DiscoverSearchBar(onSearchClick)
    }
}

@Composable
fun DiscoverMenuIcon(onMenuClick: () -> Unit){
    Icon(
        painter = painterResource(id = R.drawable.ic_menu),
        contentDescription = "抽屉菜单",
        tint = DolphinTheme.colors.icon1,
        modifier = Modifier
            .padding(start = 16.dp)
            .size(28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onMenuClick() }
    )
}
@Composable
fun RowScope.DiscoverSearchBar(onSearchClick: () -> Unit){
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(35.dp)
            .padding(horizontal = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSearchClick
            ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "网易云音乐",
                tint = Color(0x4C000000),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            val density = LocalDensity.current
            Text(
                text = "搜索音乐、视频、歌词",
                color = DolphinTheme.colors.neutral7_1,
                fontSize = with(density) { 15.dp.toSp() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}