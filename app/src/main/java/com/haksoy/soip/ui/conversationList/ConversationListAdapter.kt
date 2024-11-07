package com.haksoy.soip.ui.conversationList

import android.graphics.Typeface
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haksoy.soip.R
import com.haksoy.soip.data.ConversationWithUser
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.ConversationListItemBinding
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ConversationListAdapter(
    private val listener: ConversationListItemClickListener
) :
    RecyclerView.Adapter<ConversationListViewHolder>(), Filterable {


    private var dataList: List<ConversationWithUser> = ArrayList()
    private var dataListFiltered: List<ConversationWithUser> = ArrayList()

    interface ConversationListItemClickListener {
        fun onClickedUser(user: User)
    }

    fun setDataList(dataList: List<ConversationWithUser>) {
        this.dataList = dataList
        this.dataListFiltered = dataList.toMutableList()
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        dataList.toMutableList().removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationListViewHolder {
        val binding: ConversationListItemBinding =
            ConversationListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConversationListViewHolder(binding, listener)
    }

    override fun getItemCount(): Int = dataListFiltered.size

    override fun onBindViewHolder(holder: ConversationListViewHolder, position: Int) {
        holder.bind(dataListFiltered[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchString = constraint.toString().lowercase(Locale.getDefault()).trim()
                val filteredList = ArrayList<ConversationWithUser>()

                if (searchString.isEmpty()) {
                    filteredList.addAll(dataList)
                } else {
                    for (item in dataList) {
                        if (item.user!!.name!!.lowercase(Locale.getDefault())
                                .contains(searchString)
                        ) {
                            filteredList.add(item)
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                dataListFiltered = results?.values as List<ConversationWithUser>
                notifyDataSetChanged()
            }
        }
    }
}

class ConversationListViewHolder(
    private val itemBinding: ConversationListItemBinding,
    private val listener: ConversationListAdapter.ConversationListItemClickListener
) : RecyclerView.ViewHolder(itemBinding.root),
    View.OnClickListener {

    private lateinit var user: User

    init {
        itemBinding.root.setOnClickListener(this)
    }

    fun bind(conversationWithUser: ConversationWithUser) {
        this.user = conversationWithUser.user!!
        itemBinding.txtFullName.text = user.name
        itemBinding.txtMessage.text = conversationWithUser.conversation!!.text

        val context = itemBinding.root.context

        val cal = Calendar.getInstance()
        cal.time = Date(conversationWithUser.conversation.chatCreateDate)
        itemBinding.txtDate.text =
            SimpleDateFormat("HH:mm").format(conversationWithUser.conversation.chatCreateDate)
        Glide.with(itemBinding.root)
            .load(user.profileImage)
            .circleCrop()
            .into(itemBinding.imageView)

        if (!conversationWithUser.conversation.is_seen) {
            itemBinding.imgUnreadIndicator.visibility = View.VISIBLE
            itemBinding.txtMessage.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.chat_item_message_unread
                )
            )
            itemBinding.txtMessage.typeface = Typeface.DEFAULT_BOLD
            itemBinding.txtFullName.typeface = Typeface.DEFAULT_BOLD
        }

    }

    override fun onClick(v: View?) {
        listener.onClickedUser(user)
    }
}