package com.haksoy.soip.ui.conversationDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.databinding.ReceivedMediaItemBinding
import com.haksoy.soip.databinding.ReceivedTextItemBinding
import com.haksoy.soip.databinding.SendMediaItemBinding
import com.haksoy.soip.databinding.SendTextItemBinding
import com.haksoy.soip.ui.holdes.ReceivedMediaViewHolder
import com.haksoy.soip.ui.holdes.ReceivedTextViewHolder
import com.haksoy.soip.ui.holdes.SendMediaViewHolder
import com.haksoy.soip.ui.holdes.SendTextViewHolder

class ConversationDetailAdapter(private val listener: ConversationDetailItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<Chat>()

    fun setItems(items: ArrayList<Chat>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    interface ConversationDetailItemClickListener {
        fun onClickChat(chat: Chat)
    }

    fun removeAt(position: Int) {
        items.remove(items[position])
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ChatType.SEND_TEXT.ordinal -> return SendTextViewHolder(
                SendTextItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), listener
            )
            ChatType.RECEIVED_TEXT.ordinal -> return ReceivedTextViewHolder(
                ReceivedTextItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), listener
            )
            ChatType.SEND_VIDEO.ordinal,
            ChatType.SEND_IMAGE.ordinal -> return SendMediaViewHolder(
                SendMediaItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), listener, viewType
            )
            ChatType.RECEIVED_VIDEO.ordinal,
            ChatType.RECEIVED_IMAGE.ordinal -> return ReceivedMediaViewHolder(
                ReceivedMediaItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                listener, viewType
            )

        }
        return SendTextViewHolder(
            SendTextItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position].type) {
            ChatType.SEND_TEXT -> (holder as SendTextViewHolder).bind(items[position])
            ChatType.RECEIVED_TEXT -> (holder as ReceivedTextViewHolder).bind(items[position])
            ChatType.SEND_IMAGE, ChatType.SEND_VIDEO -> (holder as SendMediaViewHolder).bind(items[position])
            ChatType.RECEIVED_IMAGE, ChatType.RECEIVED_VIDEO -> (holder as ReceivedMediaViewHolder).bind(
                items[position]
            )

            else -> {}
        }
    }
}



