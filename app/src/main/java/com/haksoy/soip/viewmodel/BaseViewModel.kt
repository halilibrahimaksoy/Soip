package com.haksoy.soip.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.haksoy.soip.utlis.ConnectionLiveData

open class BaseViewModel(app: Application) : AndroidViewModel(app) {
    val errorMessages = MutableLiveData<String>()
    val connectionLiveData = ConnectionLiveData(app)
}