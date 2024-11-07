package com.haksoy.soip.ui.holdes

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.databinding.ImageGalleryItemBinding

class ImageGalleryViewHolder(private val binding: ImageGalleryItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private lateinit var chat: Chat
    fun bind(chat: Chat) {
        this.chat = chat
        Glide.with(binding.root).load(chat.contentUrl).fitCenter()
            .into(binding.imageView)
    }
}