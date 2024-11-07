package com.haksoy.soip.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.databinding.ImageGalleryItemBinding
import com.haksoy.soip.databinding.VideoGalleryItemBinding
import com.haksoy.soip.ui.holdes.ImageGalleryViewHolder
import com.haksoy.soip.ui.holdes.VideoGalleryViewHolder

class MediaGalleryAdapter(private val items: ArrayList<Chat>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var selectedChatPosition = -1
    fun setSelectedPosition(position: Int) {
        selectedChatPosition = position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ChatType.SEND_IMAGE.ordinal, ChatType.RECEIVED_IMAGE.ordinal -> ImageGalleryViewHolder(
                ImageGalleryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> VideoGalleryViewHolder(
                VideoGalleryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is VideoGalleryViewHolder) {
            holder.stop()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position].type) {
            ChatType.SEND_IMAGE, ChatType.RECEIVED_IMAGE -> {
                (holder as ImageGalleryViewHolder).bind(
                    items[position]
                )
            }
            ChatType.SEND_VIDEO, ChatType.RECEIVED_VIDEO -> {
                (holder as VideoGalleryViewHolder).bind(
                    items[position]
                )
                if (position == selectedChatPosition)
                    holder.play()
            }

            else -> {}
        }
    }
}