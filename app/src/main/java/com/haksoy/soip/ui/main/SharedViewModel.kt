package com.haksoy.soip.ui.main

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.haksoy.soip.data.user.User
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "SoIP:SharedViewModel"

@HiltViewModel
class SharedViewModel @Inject constructor(application: Application) : BaseViewModel(application) {
    var selectedUserList = ArrayList<User>()
    val selectedUserUid = MutableLiveData<String>()
    val selectedUser = MutableLiveData<User>()
    val conversationDetailWithUser = MutableLiveData<User>()
    val userProfileEditMode = MutableLiveData<Boolean>()

    fun getPositionFromUid(): Int {
        for (i in selectedUserList.indices) {
            if (selectedUserList[i].uid == selectedUserUid.value)
                return selectedUserList.size * ((Int.MAX_VALUE / selectedUserList.size) / 2) + i
        }
        return -1
    }

}