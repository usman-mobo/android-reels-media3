package com.reelssystem.reelsapp.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.exoplayer.source.preload.TargetPreloadStatusControl
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.trackselection.TrackSelector
import com.reelssystem.reelsapp.model.Reel

/**
 * Builds the DefaultPreloadManager, ExoPlayer LoadControl, TrackSelector,
 * and HLS MediaSource — wiring the optional disk cache (CacheDataSource)
 * into the media source factory so preloaded/playing content is cached
 * automatically.
 */
@UnstableApi
object MediaSetupFactory {

    data class PreloadSetup(
        val builder: DefaultPreloadManager.Builder,
        val preloadManager: DefaultPreloadManager,
        val reelsPreloadManager: ReelsPreloadManager
    )

    fun buildPreloadSetup(
        context: Context,
        reels: List<Reel>,
        cacheFactory: CacheDataSource.Factory?,
        preloadConfig: PreloadConfig,
        statusControl: TargetPreloadStatusControl<Int, DefaultPreloadManager.PreloadStatus>
    ): PreloadSetup {

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(1500, 5000, 500, 500)
            .setTargetBufferBytes(50 * 1024 * 1024)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val trackSelectorFactory = TrackSelector.Factory {
            DefaultTrackSelector(context).apply {
                setParameters(
                    buildUponParameters()
                        .setMaxVideoSizeSd()
                        .build()
                )
            }
        }

        val mediaSourceFactory = cacheFactory?.let { dsFactory ->
            HlsMediaSource.Factory(dsFactory)
        } ?: DefaultMediaSourceFactory(context)

        val builder = DefaultPreloadManager.Builder(context, statusControl)
            .setLoadControl(loadControl)
            .setTrackSelectorFactory(trackSelectorFactory)
            .setMediaSourceFactory(mediaSourceFactory)

        val preloadManager = builder.build()
        val reelsPreloadManager = ReelsPreloadManager(preloadManager, reels, preloadConfig)

        return PreloadSetup(builder, preloadManager, reelsPreloadManager)
    }
}