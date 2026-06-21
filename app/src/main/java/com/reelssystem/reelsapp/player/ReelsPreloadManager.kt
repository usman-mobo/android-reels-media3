package com.reelssystem.reelsapp.player

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.PreloadException
import androidx.media3.exoplayer.source.preload.PreloadManagerListener
import com.reelssystem.reelsapp.model.Reel

@UnstableApi
class ReelsPreloadManager(
    private val preloadManager: DefaultPreloadManager,
    private val reels: List<Reel>,
    private val config: PreloadConfig
) {
    private var currentPlayingPosition = 0

    companion object {
        const val TAG = "ReelsPreloadManager"
    }

    init {
        preloadManager.addListener(object : PreloadManagerListener {
            override fun onCompleted(mediaItem: MediaItem) {
                Log.d(TAG, "Completed preloading ${mediaItem.mediaId}")
            }

            override fun onError(exception: PreloadException) {
                Log.e(TAG, "Error preloading ${exception.mediaItem.mediaId}: ${exception.localizedMessage}")
            }
        })

        // Register all reels once — static list, no sliding window needed.
        // getPreloadStatus() naturally limits how much is loaded based on distance.
        reels.forEachIndexed { index, reel ->
            preloadManager.add(createMediaItem(reel), index)
        }
    }

    @Synchronized
    fun updatePreloadWindow(currentIndex: Int) {
        currentPlayingPosition = currentIndex
        preloadManager.setCurrentPlayingIndex(currentIndex)
        preloadManager.invalidate()
        Log.d(TAG, "Current playing index: $currentPlayingPosition")
    }


    fun getPreloadStatus(index: Int): DefaultPreloadManager.PreloadStatus? {
        val distance = index - currentPlayingPosition

        if (distance <= 0 || distance > config.preloadForwardRange) return null

        return when (distance) {
            1 -> DefaultPreloadManager.PreloadStatus.specifiedRangeLoaded(3000L)
            2 -> DefaultPreloadManager.PreloadStatus.specifiedRangeLoaded(1500L)
            else -> DefaultPreloadManager.PreloadStatus.SOURCE_PREPARED
        }
    }

    private fun createMediaItem(reel: Reel): MediaItem =
        MediaItem.Builder()
            .setUri(reel.hlsUrl)
            .setMediaId(reel.id)
            .build()

    fun release() {
        reels.forEach { preloadManager.remove(createMediaItem(it)) }
        preloadManager.clearListeners()
        preloadManager.release()
    }
}