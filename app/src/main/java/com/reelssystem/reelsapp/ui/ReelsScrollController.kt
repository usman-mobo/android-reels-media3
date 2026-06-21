package com.reelssystem.reelsapp.ui

import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.reelssystem.reelsapp.player.ReelsPreloadManager

/**
 * Owns playback state for the reels feed: which item is currently playing,
 * attaching/detaching the shared player as the user scrolls, and releasing
 * the player from recycled views before they're reused.
 */
@UnstableApi
class ReelsScrollController(
    private val recyclerView: RecyclerView,
    private val snapHelper: SnapHelper,
    private val adapter: ReelsAdapter,
    private val preloadManager: DefaultPreloadManager?,
    private val reelsPreloadManager: ReelsPreloadManager?,
    private val playerProvider: () -> ExoPlayer?
) {
    private var currentHolder: ReelsViewHolder? = null
    private var currentPosAdapter = RecyclerView.NO_POSITION

    // Exposed intentionally — Activity needs access to the current holder
    // Will be useful for future updates of the project
    fun currentHolder(): ReelsViewHolder? = currentHolder
    fun currentPosition(): Int = currentPosAdapter

    fun attach() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snapped = snapHelper.findSnapView(recyclerView.layoutManager) ?: return
                    val pos = recyclerView.getChildAdapterPosition(snapped)
                    if (pos != RecyclerView.NO_POSITION) {
                        playAtPosition(pos)
                        reelsPreloadManager?.updatePreloadWindow(pos)
                    }
                }
            }
        })

        recyclerView.setItemViewCacheSize(0)
        recyclerView.addRecyclerListener { holder ->
            val player = playerProvider() ?: return@addRecyclerListener
            if (holder is ReelsViewHolder && holder == currentHolder) {
                PlayerView.switchTargetView(player, holder.playerView, null)
                currentHolder = null
            }
        }
    }

    fun autoplayFirstReel() {
        val snapped = snapHelper.findSnapView(recyclerView.layoutManager) ?: return
        val pos = recyclerView.getChildAdapterPosition(snapped)
        if (pos != RecyclerView.NO_POSITION) {
            playAtPosition(pos)
        }
    }

    fun playAtPosition(pos: Int) {
        val player = playerProvider() ?: return

        if (pos == currentPosAdapter && player.playWhenReady) return

        val newHolder = recyclerView.findViewHolderForAdapterPosition(pos) as? ReelsViewHolder ?: return
        val item = adapter.getReelAt(pos) ?: return
        val newUri = item.hlsUrl.toUri()

        val curUri = player.currentMediaItem?.localConfiguration?.uri

        if (curUri != newUri) {
            val mediaItem = MediaItem.Builder()
                .setUri(newUri)
                .setMediaId(item.id)
                .build()

            val mediaSource = preloadManager?.getMediaSource(mediaItem)
            if (mediaSource != null) {
                player.setMediaSource(mediaSource)
            } else {
                player.setMediaItem(mediaItem)
            }

            player.prepare()
        }

        PlayerView.switchTargetView(player, currentHolder?.playerView, newHolder.playerView)
        player.playWhenReady = true

        currentHolder = newHolder
        currentPosAdapter = pos

        currentHolder?.hidePlayIcon()
    }
}