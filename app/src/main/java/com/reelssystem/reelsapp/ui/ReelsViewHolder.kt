package com.reelssystem.reelsapp.ui

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.reelssystem.reelsapp.R
import com.reelssystem.reelsapp.player.ReelsPlayerManager
import com.reelssystem.reelsapp.model.Reel

/**
 * View holder for a single reel item.
 *
 * Holds the PlayerView surface the shared player attaches to, plus the
 * play/pause icon overlay. Does not own playback decisions — it only
 * reflects state (show/hide icon) and forwards taps; ReelsScrollController
 * is the single source of truth for what's currently playing and where.
 */
@UnstableApi
class ReelsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = itemView.findViewById(R.id.title)
    private val description: TextView = itemView.findViewById(R.id.description)
    val playerView: PlayerView = itemView.findViewById(R.id.player)
    val playIcon: ImageView = itemView.findViewById(R.id.play_icon)

    init {
        playerView.setKeepContentOnPlayerReset(true)
        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
    }

    fun bind(reel: Reel, isCurrentlyPlaying: () -> Boolean) {
        title.text = reel.title

        playerView.setOnClickListener {
            val player = ReelsPlayerManager.player ?: return@setOnClickListener
            if (!isCurrentlyPlaying()) return@setOnClickListener

            if (player.isPlaying) {
                player.playWhenReady = false
                showPlayIcon()
            } else {
                player.playWhenReady = true
                hidePlayIcon()
            }
        }
    }

    fun hideViewsOnFirstFrame(){
        playIcon.alpha = 0f
    }

    private fun showPlayIcon() {
        playIcon.clearAnimation()
        playIcon.apply {
            scaleX = 0.6f
            scaleY = 0.6f
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(200)
                .start()
        }

        val player = ReelsPlayerManager.player
        if (player != null && player.isPlaying) {
            playIcon.postDelayed({
                playIcon.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction { playIcon.visibility = View.GONE }
                    .start()
            }, 1500)
        }
    }

    fun hidePlayIcon() {
        playIcon.visibility = View.GONE
        playIcon.alpha = 0f
    }

    fun cleanUp() {
        playIcon.clearAnimation()
        playIcon.visibility = View.GONE
        playIcon.alpha = 0f
    }

}