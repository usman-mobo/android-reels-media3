package com.reelssystem.reelsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.reelssystem.reelsapp.R
import com.reelssystem.reelsapp.model.Reel


/**
 * One row in the reels feed. Static list, plain RecyclerView.Adapter —
 * no DiffUtil/Paging needed since the dataset never changes after load.
 * In production (infinite feed), this would be a PagingDataAdapter with
 * DiffUtil-driven updates instead.
 */
@UnstableApi
class ReelsAdapter(
    private val isCurrentlyPlaying: (position: Int) -> Boolean
) : RecyclerView.Adapter<ReelsViewHolder>() {

    private var dataset: List<Reel> = ArrayList()

    fun setDataset(dataset: List<Reel>){
        this.dataset = dataset
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_reel, parent, false)
        return ReelsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelsViewHolder, position: Int) {
        holder.bind(dataset[position]) { isCurrentlyPlaying(position) }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    fun getReelAt(position: Int): Reel? = dataset.getOrNull(position)

    override fun onViewRecycled(holder: ReelsViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanUp()
    }
}