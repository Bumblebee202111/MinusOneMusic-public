package com.github.bumblebee202111.minusonecloudmusic.ui.mine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.R
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemMyMusicDragonBallBinding
import kotlin.math.roundToInt

class DragonBallAdapter(
    private val dragonBalls: List<MineDragonBall>,
    private val onItemClick: (MineDragonBall) -> Unit
) :
    RecyclerView.Adapter<DragonBallAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ListItemMyMusicDragonBallBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dragonBall: MineDragonBall, itemOnClickListener: View.OnClickListener) {
            binding.dragonBall = dragonBall
            binding.root.setOnClickListener(itemOnClickListener)
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemMyMusicDragonBallBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val resources = parent.context.resources
        val itemCount = itemCount
        binding.root.layoutParams.width =
            ((parent.measuredWidth - resources.getDimension(
                R.dimen.dragon_ball_spacing
            )* (itemCount - 1)) / itemCount).roundToInt()
        return ViewHolder(binding)
    }

    override fun getItemCount() = dragonBalls.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dragonBall = dragonBalls[position]
        holder.bind(dragonBall) {
            onItemClick(dragonBall)
        }
    }
}