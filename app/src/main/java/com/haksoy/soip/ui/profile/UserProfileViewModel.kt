package com.haksoy.soip.ui.profile

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.database.UserRepository
import com.haksoy.soip.data.user.User
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.utlis.observeOnce
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    app: Application,
    private val userRepository: UserRepository,
    val firebaseDao: FirebaseDao
) : BaseViewModel(app) {
    val currentUser = MutableLiveData<User>()
    fun getUid(): String {
        return firebaseDao.getCurrentUserUid()
    }

    fun getPhoneNumber(): String {
        return firebaseDao.getCurrentUserPhoneNumber()
    }

    fun fetchUserData() {
        firebaseDao.fetchUserDate(currentUser.value!!.uid).observeOnce {
            if (it.status == Resource.Status.SUCCESS) {
                currentUser.postValue(it.data!!)
                addUser(it.data)
            } else if (it.status == Resource.Status.ERROR) {
                errorMessages.postValue(it.message)
            }
        }
    }

    fun fetchUserDataFromLocale(uid: String) {
        userRepository.getUser(uid).observeOnce {
            if (it.status == Resource.Status.SUCCESS) {
                currentUser.postValue(it.data!!)
            } else if (it.status == Resource.Status.ERROR) {
                errorMessages.postValue(it.message)
            }
        }
    }

    private fun addUser(user: User)  {
        userRepository.addUser(user)
    }

    fun updateUserProfile(user: User): MutableLiveData<Resource<Exception>> {
        user.updatedDate = System.currentTimeMillis()
        return firebaseDao.updateUserProfile(user)
    }
}