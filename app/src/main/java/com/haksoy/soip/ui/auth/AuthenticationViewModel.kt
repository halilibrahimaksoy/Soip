package com.haksoy.soip.ui.auth

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.haksoy.soip.BuildConfig
import com.haksoy.soip.R
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.utlis.ConnectionLiveData
import com.haksoy.soip.utlis.ProgressHelper
import com.haksoy.soip.utlis.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "SoIP:AuthenticationViewModel"


@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    application: Application,
    val firebaseDao: FirebaseDao
) : AndroidViewModel(application) {
    val verificationId = MutableLiveData<String>()
    val errorMessages = MutableLiveData<String>()
    val connectionLiveData = ConnectionLiveData(application)

    @Inject
    lateinit var progressHelper: ProgressHelper

    private var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                progressHelper.hideLoading()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                progressHelper.hideLoading()
                if (e is FirebaseAuthInvalidCredentialsException) {
                    errorMessages.postValue(application.getString(R.string.invalid_phone_number))
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else {
                    if (BuildConfig.DEBUG) {
                        errorMessages.postValue(e.localizedMessage)
                    }
                }

            }

            override fun onCodeSent(
                _verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:${verificationId.value}")
                progressHelper.hideLoading()
                verificationId.postValue(_verificationId)
            }
        }

    fun verifyPhoneNumber(activity: Activity, phoneNumber: String) {
        firebaseDao.verifyPhoneNumber(activity, phoneNumber, callbacks)
    }

    fun signInWithPhoneAuthCredential(
        verificationId: String,
        otp: String
    ): LiveData<Resource<String>> {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        return firebaseDao.signInWithPhoneAuthCredential(credential)
    }


}