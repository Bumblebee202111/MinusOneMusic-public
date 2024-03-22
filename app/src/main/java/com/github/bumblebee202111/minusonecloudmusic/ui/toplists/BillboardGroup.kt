package com.github.bumblebee202111.minusonecloudmusic.ui.toplists

import com.github.bumblebee202111.minusonecloudmusic.data.model.Playlist

data class BillBoardGroup(
    val isOfficial:Boolean,
    val billboards:List<Playlist>
)