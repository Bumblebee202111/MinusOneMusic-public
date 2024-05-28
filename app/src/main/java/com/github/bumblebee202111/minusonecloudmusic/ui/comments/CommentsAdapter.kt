package com.github.bumblebee202111.minusonecloudmusic.ui.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.bumblebee202111.minusonecloudmusic.data.model.Comment
import com.github.bumblebee202111.minusonecloudmusic.databinding.ListItemCommentBinding

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.ViewHolder>(Comment.DIFF_CALLBACK) {
    class ViewHolder(private val binding: ListItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                setComment(comment)
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}