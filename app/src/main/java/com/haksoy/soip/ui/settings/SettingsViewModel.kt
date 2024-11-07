package com.haksoy.soip.ui.settings

import android.app.Application
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.database.ChatRepository
import com.haksoy.soip.viewmodel.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    app: Application,
    val chatRepository: ChatRepository,
    val firebaseDao: FirebaseDao
) : BaseViewModel(app) {
    fun signOut() {
        firebaseDao.auth.signOut()
    }
}