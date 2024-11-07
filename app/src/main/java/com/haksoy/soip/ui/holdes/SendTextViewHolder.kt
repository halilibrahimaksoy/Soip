package com.haksoy.soip.ui.holdes

import android.icu.text.SimpleDateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.Status
import com.haksoy.soip.databinding.SendTextItemBinding
import com.haksoy.soip.ui.conversationDetail.ConversationDetailAdapter
import java.util.Calendar
import java.util.Date

class SendTextViewHolder(
    private val binding: SendTextItemBinding,
    private val listener: ConversationDetailAdapter.ConversationDetailItemClickListener
) : RecyclerView.ViewHolder(binding.root),
    View.OnClickListener {

    init {
        binding.root.setOnClickListener(this)
    }

    private lateinit var chat: Chat
    fun bind(chat: Chat) {
        this.chat = chat
        binding.txtMessage.text = chat.getText()
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