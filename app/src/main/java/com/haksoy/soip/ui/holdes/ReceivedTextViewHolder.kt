package com.haksoy.soip.ui.holdes

import android.icu.text.SimpleDateFormat
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.databinding.ReceivedTextItemBinding
import com.haksoy.soip.ui.conversationDetail.ConversationDetailAdapter
import java.util.*

class ReceivedTextViewHolder(
    private val leftBinding: ReceivedTextItemBinding,
    private val listener: ConversationDetailAdapter.ConversationDetailItemClickListener
) : RecyclerView.ViewHolder(leftBinding.root),
    View.OnClickListener {

    init {
        leftBinding.root.setOnClickListener(this)
    }

    private lateinit var chat: Chat
    fun bind(chat: Chat) {
        this.chat = chat
        leftBinding.txtMessage.text = chat.getText()
        val cal = Calendar.getInstance()
        cal.time = Date(chat.createDate)
        leftBinding.txtDate.text = SimpleDateFormat("HH:mm").format(chat.createDate)


    }

    override fun onClick(v: View?) {
        listener.onClickChat(chat)
    }
}