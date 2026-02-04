package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.viewinterop.AndroidViewBinding
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemMyMusicDragonBallBinding

@Composable
fun DragonBallRow(
    dragonBalls: List<MineDragonBall>,
    onItemClick: (MineDragonBall) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dragon_ball_spacing))
    ) {
        dragonBalls.forEach { dragonBall ->
            Box(modifier = Modifier.weight(1f)) {
                AndroidViewBinding(
                    factory = ListItemMyMusicDragonBallBinding::inflate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (this.root.layoutParams.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                        this.root.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    
                    this.dragonBall = dragonBall
                    this.root.setOnClickListener { onItemClick(dragonBall) }
                    this.executePendingBindings()
                }
            }
        }
    }
}
