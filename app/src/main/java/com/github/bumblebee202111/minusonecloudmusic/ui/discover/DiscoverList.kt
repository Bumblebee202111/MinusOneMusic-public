import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.github.bumblebee202111.minusonecloudmusic.model.DiscoverBlock
import com.github.bumblebee202111.minusonecloudmusic.ui.discover.DragonBallBlockView
import com.github.bumblebee202111.minusonecloudmusic.ui.discover.PlaylistBlockView
import com.github.bumblebee202111.minusonecloudmusic.ui.discover.TopListBlockView
import com.github.bumblebee202111.minusonecloudmusic.ui.playlist.PlaylistFragment

@Composable
fun DiscoverList(
    blocks: List<DiscoverBlock>,
    onDragonBallClick: (DiscoverBlock.DragonBalls.DragonBall.Type) -> Unit,
    onPlaylistClick: (playlistId: Long, playlistCreatorId: Long, isMyPL: Boolean) -> Unit
) {
    LazyColumn {
        items(blocks) { block ->
            when (block) {
                is DiscoverBlock.DragonBalls -> {
                    DragonBallBlockView(block, onDragonBallClick)
                }

                is DiscoverBlock.Playlists -> {
                    PlaylistBlockView(block) { id, creatorId ->
                        onPlaylistClick(id, creatorId, true)
                    }
                }

                is DiscoverBlock.TopLists -> {
                    TopListBlockView(block) { id ->
                        onPlaylistClick(
                            id,
                            PlaylistFragment.ARG_VALUE_PLAYLIST_CREATOR_ID_UNKNOWN,
                            false
                        )
                    }
                }
            }
        }
    }
}