package com.github.bumblebee202111.minusonecloudmusic.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.InboxRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.navigation.SettingsRoute
import com.github.bumblebee202111.minusonecloudmusic.ui.theme.DolphinTheme


@Composable
fun MainDrawerContent(
    onNavigate: (NavKey) -> Unit,
    onLogout: () -> Unit
) {

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.84f),
        drawerShape = RectangleShape,
        drawerContainerColor = DolphinTheme.colors.backgroundAndroid,
        drawerContentColor = DolphinTheme.colors.text2
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
        ) {


            DrawerCard {
                DrawerItem(
                    label = R.string.menu_inbox,
                    onClick = { onNavigate(InboxRoute) }
                )
            }

            DrawerCard {
                DrawerItem(
                    label = R.string.menu_settings,
                    onClick = { onNavigate(SettingsRoute) }
                )


                DrawerItem(
                    label = R.string.menu_log_out,
                    onClick = onLogout
                )
            }
        }
    }
}

@Composable
private fun DrawerCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = DolphinTheme.colors.backgroundDual,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
    ) {
        Column(
            content = content
        )
    }
}

@Composable
private fun DrawerItem( label: Int, onClick:()-> Unit){
    Row(
        modifier = Modifier.height(48.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = null,
            tint = DolphinTheme.colors.icon2,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(label),
            modifier = Modifier.weight(1f),
            color = DolphinTheme.colors.text2,
            fontSize = 13.sp,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}