package com.reelssystem.reelsapp.cache


data class ReelsConfig (
    val cacheSizeBytes: Long = 30L * 1024 * 1024,
    val userAgent: String = "ShunyeApp/1.0",
    val connectTimeoutMs: Int = 15_000,
    val readTimeoutMs: Int = 15_000
)