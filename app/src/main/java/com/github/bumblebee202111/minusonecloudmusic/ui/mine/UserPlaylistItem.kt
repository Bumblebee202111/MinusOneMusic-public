package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import com.github.bumblebee202111.minusonecloudmusic.model.Playlist
sealed interface UserPlaylistItem
class NormalPlaylistItem(val playlist: Playlist):UserPlaylistItem
class UserChartsItem(val userId:Long, val listenSongs:Long):UserPlaylistItem