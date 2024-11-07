package com.haksoy.soip.ui.holdes

import android.icu.text.SimpleDateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.data.chat.Status
import com.haksoy.soip.databinding.SendMediaItemBinding
import com.haksoy.soip.ui.conversationDetail.ConversationDetailAdapter
import java.util.Calendar
import java.util.Date


class SendMediaViewHolder(
    private val binding: SendMediaItemBinding,
    private val listener: ConversationDetailAdapter.ConversationDetailItemClickListener,
    private val viewType: Int
) : RecyclerView.ViewHolder(binding.root),
    View.OnClickListener {

    init {
        binding.root.setOnClickListener(this)
    }

    private lateinit var chat: Chat
    fun bind(chat: Chat) {
        this.chat = chat
        if (viewType == ChatType.SEND_IMAGE.ordinal) {
            Glide.with(binding.root).load(chat.contentUrl).into(binding.imgMsg)
        } else {

            Glide.with(binding.root)
                .load(chat.contentUrl)
                .centerCrop()
                .into(binding.imgMsg)

            binding.imgPlay.visibility = View.VISIBLE
        }
        val cal = Calendar.getInstance()
        cal.time = Date(chat.createDate)
        binding.txtDate.text = SimpleDateFormat("HH:mm").format(chat.createDate)

        if (chat.status == Status.LOADING)
            binding.loading.visibility = View.VISIBLE
        else
            binding.loading.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        listener.onClickChat(chat)
    }
}