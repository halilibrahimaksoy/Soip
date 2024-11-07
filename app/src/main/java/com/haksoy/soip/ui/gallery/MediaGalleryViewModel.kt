package com.haksoy.soip.ui.gallery

import android.app.Application
import com.haksoy.soip.data.database.ChatRepository
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MediaGalleryViewModel @Inject constructor(
    app: Application,
    private val chatRepository: ChatRepository
) :
    BaseViewModel(app) {
    fun getConversationMedia(uid: String) = chatRepository.getConversationMedia(uid)
}