package com.haksoy.soip.ui.holdes

import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.databinding.VideoGalleryItemBinding


class VideoGalleryViewHolder(private val binding: VideoGalleryItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private lateinit var player: SimpleExoPlayer
    private lateinit var chat: Chat

    fun bind(chat: Chat) {
        this.chat = chat
        player = SimpleExoPlayer.Builder(binding.root.context).build()
        binding.videoView.player = player
        val mediaItem: MediaItem = MediaItem.fromUri(chat.contentUrl!!)
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun play() {
        player.playWhenReady = true
    }

    fun stop() {
        player.stop()
    }
}