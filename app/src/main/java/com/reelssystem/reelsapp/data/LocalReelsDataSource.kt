package com.reelssystem.reelsapp.data

import com.reelssystem.reelsapp.model.Reel

/**
 * Static list of sample reels used by this demo.
 *
 * All 10 clips are hosted on Mux and encoded with a consistent profile
 * (bitrate, resolution, segment duration) so preload/cache timing is
 * comparable across items — see README for encoding details. In
 * production this would be backed by a real API/CMS instead of a
 * hardcoded list.
 */
object LocalReelsDataSource {


    private const val BASE_URL = "https://stream.mux.com"

    fun getReels(): List<Reel> = listOf(
        Reel(id = "1", hlsUrl = "$BASE_URL/r701jhU7Qt9eUrTTaj8ednmhsDmAAQ402NeNh8i00BUYz8.m3u8", title = "Reel 1"),
        Reel(id = "2", hlsUrl = "$BASE_URL/Lv1X01Ga2OE4diQSiZ5YowFz6hNjgGI6jbWMe6rvyA2c.m3u8", title = "Reel 2"),
        Reel(id = "3", hlsUrl = "$BASE_URL/hYr00AnkD6h2yTgofUH9R4iPeC9Gs02HGk4f4MzCLbMPw.m3u8", title = "Reel 3"),
        Reel(id = "4", hlsUrl = "$BASE_URL/004jleLl01VYSk021h193a02JP01PbEBcwiBJCrIi1mBCD00E.m3u8", title = "Reel 4"),
        Reel(id = "5", hlsUrl = "$BASE_URL/z5dj24QYGS2B021zFVrReu31ZO2yKLHUWfi2Myu00McGA.m3u8", title = "Reel 5"),
        Reel(id = "6", hlsUrl = "$BASE_URL/4zfh3OgxaXpgDqRn2dmXUpR4tYf3cR02gVc01hBq9Jjtc.m3u8", title = "Reel 6"),
        Reel(id = "7", hlsUrl = "$BASE_URL/n9X1isuY82NpVIpObJeMIl7dFu3shBqHkHFK5GYJmDo.m3u8", title = "Reel 7"),
        Reel(id = "8", hlsUrl = "$BASE_URL/Om8DvqQ1KANFKzEZHILy874Q9XsmMwnDupXGhczdpfQ.m3u8", title = "Reel 8"),
        Reel(id = "9", hlsUrl = "$BASE_URL/Rr9Ck02Zv4Ufr5rbHu2hX5uTJaUDeyBWi5IZkSrXpQtU.m3u8", title = "Reel 9"),
        Reel(id = "10", hlsUrl = "$BASE_URL/702mzTBpWAwTJHEKg9Nu3nYwhIkTYYHyqYAQml9D7Fv00.m3u8", title = "Reel 10"),
    )

}