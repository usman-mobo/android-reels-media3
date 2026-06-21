package com.reelssystem.reelsapp.player

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager

/**
 * Single shared ExoPlayer instance used across all reel items.
 *
 * Reels use one player that gets attached/detached between RecyclerView
 * item views as the user scrolls (see ReelsScrollController), instead of
 * each item owning its own player. This keeps memory and decoder usage
 * bounded regardless of list size.
 */
@UnstableApi
object ReelsPlayerManager {
    var player: ExoPlayer? = null
        private set

    fun init(preloadManagerBuilder: DefaultPreloadManager.Builder) {
        if (player != null) return

        player = preloadManagerBuilder.buildExoPlayer()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                trackSelectionParameters = trackSelectionParameters
                    .buildUpon()
                    .setMaxVideoSizeSd()
                    .build()
            }


    }

    fun release() { player?.release(); player = null }


}