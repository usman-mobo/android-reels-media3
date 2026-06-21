package com.reelssystem.reelsapp.cache

import android.content.Context
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Owns disk caching of HLS segments via SimpleCache + CacheDataSource.
 *
 * Caching here is purely reactive: bytes get written as a side effect of
 * being read during playback. This is what makes scrolling back to an
 * already-watched reel instant, independent of the forward-only preload
 * manager above.
 */
@UnstableApi
object CacheManagerReels {
    private var config = ReelsConfig()
    @Volatile private var cacheInstance: SimpleCache? = null
    @Volatile var upstreamDataSourceFactory: DefaultHttpDataSource.Factory? = null
    @Volatile private var cacheDsFactory: CacheDataSource.Factory? = null

    fun init(context: Context, config: ReelsConfig) {
        if (cacheInstance != null) return
        synchronized(this){
            if (cacheInstance != null) return
            this.config = config
            val exoCacheDir = File("${context.applicationContext.cacheDir.absolutePath}/reels")
            val evictor = LeastRecentlyUsedCacheEvictor(config.cacheSizeBytes)
            cacheInstance = SimpleCache(exoCacheDir, evictor,
                StandaloneDatabaseProvider(context.applicationContext))
            cacheInstance?.addListener("", CacheListenerReels())

            upstreamDataSourceFactory = DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(config.connectTimeoutMs)
                .setReadTimeoutMs(config.readTimeoutMs)
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent(config.userAgent)

            cacheDsFactory = CacheDataSource.Factory()
                .setCache(requireNotNull(requireNotNull(cacheInstance)))
                .setCacheWriteDataSinkFactory(
                    CacheDataSink.Factory()
                        .setCache(requireNotNull(requireNotNull(cacheInstance)))
                        .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
                )
                .setUpstreamDataSourceFactory(requireNotNull(upstreamDataSourceFactory))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                .setEventListener(CacheDataSourceListenerReels())

        }
    }

    fun getSimpleCache() = requireNotNull(cacheInstance) { "Call CacheManagerReels.init() first" }

    fun getHttpDataSourceFactory() = requireNotNull(upstreamDataSourceFactory) { "Call CacheManagerReels.init() first" }

    fun release() {
        synchronized(this) {
            cacheInstance?.release()
            cacheInstance = null
            upstreamDataSourceFactory = null
            cacheDsFactory = null
        }
    }

    fun getCacheDataSourceFactory(): CacheDataSource.Factory =
        requireNotNull(cacheDsFactory) { "Call CacheManagerReels.init() first" }


    class CacheListenerReels : Cache.Listener{
        override fun onSpanAdded(cache: Cache, span: CacheSpan) {
            Log.d("chtag", "onSpanAdded: ${span.key} - ${span.position} - ${span.length}")
        }

        override fun onSpanRemoved(cache: Cache, span: CacheSpan) {
            Log.d("chtag", "onSpanRemoved: ${span.key} - ${span.position} - ${span.length}")
        }

        override fun onSpanTouched(cache: Cache, oldSpan: CacheSpan, newSpan: CacheSpan) {
            Log.d("chtag", "onSpanTouched: ${oldSpan.key} - ${oldSpan.position} - ${oldSpan.length}")
        }
    }

    class CacheDataSourceListenerReels: CacheDataSource.EventListener{

        override fun onCachedBytesRead(cacheSizeBytes: Long, cachedBytesRead: Long) {
            Log.d("chtag", "onCachedBytesRead: cacheSizeBytes = $cacheSizeBytes")
            Log.d("chtag", "onCachedBytesRead: cachedBytesRead = $cachedBytesRead")
        }

        override fun onCacheIgnored(reason: Int) {
            Log.d("chtag", "onCacheIgnored: reason = $reason")

        }

    }

}