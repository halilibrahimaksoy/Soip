package com.haksoy.soip.ui.holdes

import androidx.media3.common.Player
import androidx.recyclerview.widget.RecyclerView
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.databinding.VideoGalleryItemBinding


class VideoGalleryViewHolder(private val binding: VideoGalleryItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private lateinit var player: Player
    private lateinit var chat: Chat

    fun bind(chat: Chat) {
        this.chat = chat
//        player = Player.Builder(binding.root.context).build()
//        binding.videoView.player = player
//        val mediaItem: MediaItem = MediaItem.fromUri(chat.contentUrl!!)
//        player.setMediaItem(mediaItem)
//        player.prepare()
    }

    fun play() {
        player.playWhenReady = true
    }

    fun stop() {
        player.stop()
    }
}