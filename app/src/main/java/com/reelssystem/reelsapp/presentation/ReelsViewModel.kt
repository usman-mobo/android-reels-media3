package com.reelssystem.reelsapp.presentation

import androidx.lifecycle.ViewModel
import com.reelssystem.reelsapp.data.ReelsRepository
import com.reelssystem.reelsapp.model.Reel

class ReelsViewModel : ViewModel() {
    private val repository = ReelsRepository()
    val reels: List<Reel> = repository.getReels()
}