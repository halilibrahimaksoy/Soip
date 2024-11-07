package com.haksoy.soip.data.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.user.User
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.utlis.observeOnce
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepository @Inject constructor(
    appDatabase: AppDatabase,
    val firebaseDao: FirebaseDao
) {
    private val userDao = appDatabase.userDao()

    fun getUser(uid: String): LiveData<Resource<User>> {
        val result = MutableLiveData<Resource<User>>()
        userDao.getUserData(uid).observeForever {
            if (it != null)
                result.value = Resource.success(it)
            else {
                firebaseDao.fetchUserDate(uid).observeOnce { remoteResult ->
                    if (remoteResult.status == Resource.Status.SUCCESS) {
                        result.value = remoteResult
                        GlobalScope.launch {
                            addUser(remoteResult.data!!)
                        }

                    } else if (remoteResult.status == Resource.Status.ERROR) {
                        result.value = Resource.error(remoteResult.message!!)
                    }
                }
            }
        }
        return result
    }

    fun addUser(user: User) {
        user.createDate = System.currentTimeMillis()
        userDao.addUser(user)
    }
}