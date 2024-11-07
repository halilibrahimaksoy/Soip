package com.haksoy.soip.ui.main

import android.app.Application
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.database.UserRepository
import com.haksoy.soip.data.user.User
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    app: Application,
    private val userRepository: UserRepository,
    val firebaseDao: FirebaseDao
) : BaseViewModel(app) {

    val isUserDataExist = firebaseDao.isUserDataExist(firebaseDao.getCurrentUserUid())

    fun getUser(userUid: String) =
        userRepository.getUser(userUid)

    fun addUser(user: User) {
        userRepository.addUser(user)
    }
}