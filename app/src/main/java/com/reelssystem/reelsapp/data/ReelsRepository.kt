package com.reelssystem.reelsapp.data

import com.reelssystem.reelsapp.model.Reel

/**
 * Thin pass-through to LocalReelsDataSource.
 *
 * Kept intentionally simple — no Flow, no Result wrapper, no interface —
 * since this demo's data is static and local. In production this would
 * sit behind a repository interface backed by Room/Retrofit, returning
 * Flow<List<Reel>> instead of a plain List.
 */
class ReelsRepository {
    fun getReels(): List<Reel> = LocalReelsDataSource.getReels()
    // In production: inject DataSource, return Flow<List<Reel>> from Room/Retrofit
}