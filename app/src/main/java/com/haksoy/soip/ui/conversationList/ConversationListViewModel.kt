package com.haksoy.soip.ui.conversationList

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.haksoy.soip.data.database.ChatRepository
import com.haksoy.soip.utlis.observeOnce
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ConversationListViewModel"

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    app: Application,
    val chatRepository: ChatRepository
) :
    BaseViewModel(app) {
    fun getConversationWithUserList() = chatRepository.getConversationWithUserList()


    fun removeConversationAtPosition(position: Int) {
        Log.i(TAG, "removeConversationAtPosition: position at $position")
        chatRepository.getConversationWithUserList().observeOnce {
            viewModelScope.launch {
                chatRepository.removeConversation(it[position].conversation!!.userUid)
            }

        }
    }

}