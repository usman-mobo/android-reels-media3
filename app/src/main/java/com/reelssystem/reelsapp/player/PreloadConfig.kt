package com.reelssystem.reelsapp.player

/**
 * Controls how far ahead (in items) preloading should reach, and how
 * aggressively to load each step. Forward-only by design — preloading
 * targets *upcoming* reels; previously watched reels rely on disk cache
 * (see CacheManagerReels) for instant scroll-back instead.
 */
data class PreloadConfig(
    val preloadForwardRange: Int,
)