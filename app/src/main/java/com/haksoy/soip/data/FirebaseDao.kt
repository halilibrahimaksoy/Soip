package com.haksoy.soip.data

import android.app.Activity
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.haksoy.soip.data.user.User
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.utlis.observeOnce
import java.io.File
import java.util.concurrent.TimeUnit

private const val TAG = "SoIP:FirebaseDao"

class FirebaseDao {


    val auth = Firebase.auth
    private val cloudFirestoreDB = Firebase.firestore
    private val storageFirebase = Firebase.storage
    private val realtimeDB = FirebaseDatabase.getInstance().getReference("Location")
    private val geoFire = GeoFire(realtimeDB)


    fun verifyPhoneNumber(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)// Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(credentials: PhoneAuthCredential): LiveData<Resource<String>> {
        val result = MutableLiveData<Resource<String>>()

        auth.signInWithCredential(credentials)
            .addOnSuccessListener {
                result.value = Resource.success(it.user!!.uid)
                updateToken()
            }
            .addOnFailureListener {
                result.value = Resource.error(it.localizedMessage, it.toString())
            }
        return result
    }

    fun getCurrentUserUid(): String {
        return auth.currentUser!!.uid
    }

    fun getCurrentUserPhoneNumber(): String {
        return auth.currentUser!!.phoneNumber.toString()
    }

    fun setLanguageCode(languageCode: String) {
        auth.setLanguageCode(languageCode)
    }

    fun fetchUserDate(uid: String): LiveData<Resource<User>> {
        val docRef = cloudFirestoreDB.collection(Constants.User).document(uid)
        val result = MutableLiveData<Resource<User>>()
        docRef.get().addOnCompleteListener {
            if (it.isSuccessful) {
                result.value = Resource.success(it.result.toObject(User::class.java))
            } else
                result.value = Resource.error(it.exception!!.localizedMessage)
        }
        return result
    }

    fun updateUserProfile(user: User): MutableLiveData<Resource<Exception>> {
        val result = MutableLiveData<Resource<Exception>>()
        if (!user.profileImage.toString()
                .contains(Constants.firebaseStoregeURL, false)
        ) {// for understanding to is image changed ?
            uploadProfileImage(getCurrentUserUid(), user.profileImage.toString()).observeOnce {
                if (it.status == Resource.Status.SUCCESS) {
                    user.profileImage = it.data
                    updateUser(
                        user
                    ).observeOnce { it1 ->
                        if (it1.status == Resource.Status.SUCCESS) {
                            result.value = Resource.success(null)
                        } else if (it1.status == Resource.Status.ERROR) {
                            result.value = Resource.error(it1.message!!)
                        }
                    }
                } else if (it.status == Resource.Status.ERROR) {
                    result.value = Resource.error(it.message!!)
                }
            }
        } else {
            updateUser(
                user
            ).observeOnce {
                if (it.status == Resource.Status.SUCCESS) {
                    result.value = Resource.success(null)
                } else if (it.status == Resource.Status.ERROR) {
                    result.value = Resource.error(it.message!!)
                }
            }
        }
        return result
    }

    private fun updateUser(
        user: User
    ): MutableLiveData<Resource<Exception>> {
        val result = MutableLiveData<Resource<Exception>>()
        cloudFirestoreDB.collection(Constants.User).document(user.uid).set(user)
            .addOnSuccessListener {
                result.value = Resource.success(null)
            }
            .addOnFailureListener {
                result.value = Resource.error(it.localizedMessage, it)
            }

        return result
    }

    private fun uploadProfileImage(
        uid: String,
        imageUri: String
    ): MutableLiveData<Resource<String>> {
        val result = MutableLiveData<Resource<String>>()
        val updateRef = storageFirebase.reference.child(Constants.User_Profile_Image).child(uid)
        val uploadTask = updateRef.putFile(Uri.parse(imageUri))

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    result.value = Resource.error(it.localizedMessage)
                }
            }
            updateRef.downloadUrl
        }.addOnSuccessListener {
            result.value = Resource.success(it.toString())
        }.addOnFailureListener {
            result.value = Resource.error(it.localizedMessage)
        }

        return result
    }

    fun uploadMedia(
        fileName: String,
        imageUri: String
    ): MutableLiveData<Resource<String>> {
        val result = MutableLiveData<Resource<String>>()
        val updateRef = storageFirebase.reference.child(Constants.MEDIA).child(fileName)
        val uploadTask = updateRef.putFile(Uri.fromFile(File(imageUri)))

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    result.value = Resource.error(it.localizedMessage)
                }
            }
            updateRef.downloadUrl
        }.addOnSuccessListener {
            result.value = Resource.success(it.toString())
        }.addOnFailureListener {
            result.value = Resource.error(it.localizedMessage)
        }

        return result
    }

    fun updateToken(token: String) {
        cloudFirestoreDB.collection(Constants.User).document(getCurrentUserUid())
            .update("token", token).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG, "updateToken: Successful  -> $token")
                } else {
                    Log.i(TAG, "updateToken: failed")
                }
            }
    }

    private fun updateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            cloudFirestoreDB.collection(Constants.User).document(getCurrentUserUid())
                .update("token", it.result).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "updateToken: Successful  -> ${it.result}")
                    } else {
                        Log.i(TAG, "updateToken: failed")
                    }
                }
        }
    }

    fun isAuthUserExist() = auth.currentUser != null

    val currentLocation = MutableLiveData<GeoLocation>()
    fun getLocation(uid: String) {
        realtimeDB.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "getLocation: onCancelled")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null)
                    currentLocation.postValue(GeoFire.getLocationValue(snapshot))
            }
        })
    }

    val nearlyUser = MutableLiveData<Resource<List<User>>>()
    private val nearlyLocationLiveDataObserver =
        Observer<HashMap<String, GeoLocation>> { nearlyLocation ->
            val userResultList = ArrayList<User>()
            val nearlyUserKeyList = nearlyLocation.keys.reversed()

            for (i in 0 until nearlyLocation.size step Constants.userFetchStep) {
                val endPoint =
                    if (i + Constants.userFetchStep <= nearlyLocation.size) i + Constants.userFetchStep else nearlyLocation.size
                Log.i(TAG, "getNearlyUsers: get subList from $i between $endPoint")
                val subList = nearlyUserKeyList.subList(i, endPoint)
                cloudFirestoreDB.collection(Constants.User).whereIn(Constants.uid, subList)
                    .get().addOnCompleteListener { it1 ->
                        if (it1.isSuccessful) {
                            val userTempList = it1.result.toObjects(User::class.java)
                            Log.i(TAG, "getNearlyUsers: fetched nearly users")
                            for (user in userTempList) {
                                Log.i(TAG, "getNearlyUsers: added location to ${user.uid}")
                                user.location = com.haksoy.soip.data.user.Location(
                                    latitude = nearlyLocation[user.uid]!!.latitude,
                                    longitude = nearlyLocation[user.uid]!!.longitude
                                )
                            }
                            userResultList.addAll(userTempList)
                            Log.i(TAG, "getNearlyUsers: postValue userResultList")
                            nearlyUser.postValue(Resource.success(userResultList))
                        } else {
                            Log.i(TAG, "getNearlyUsers: postValue error")
                            nearlyUser.postValue(Resource.error(it1.exception!!.localizedMessage))
                        }
                    }
            }

        }

    fun startNearlyLocationObserve(location: GeoLocation) {
        getNearlyLocations(location)
        nearlyLocationLiveData.observeForever(nearlyLocationLiveDataObserver)
    }

    fun stopNearlyLocationObserve() {
        Log.i(TAG, "stopNearlyLocationObserve")
        nearlyLocationLiveData.removeObserver(nearlyLocationLiveDataObserver)
    }

    private val nearlyLocationLiveData = MutableLiveData<HashMap<String, GeoLocation>>()
    private fun getNearlyLocations(location: GeoLocation) {
        val nearlyLocation = LinkedHashMap<String, GeoLocation>()
        geoFire.queryAtLocation(location, Constants.nearlyLimit)
            .addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onGeoQueryReady() {
                    Log.i(TAG, "getNearlyLocations: onGeoQueryReady")
                    Log.i(TAG, "getNearlyLocations:  postValue nearlyLocation")
                    nearlyLocationLiveData.postValue(nearlyLocation)
                }

                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    Log.i(TAG, "getNearlyLocations: onKeyEntered : $key")
                    if (key != getCurrentUserUid()) {
                        nearlyLocation[key!!] = location!!
                        nearlyLocationLiveData.postValue(nearlyLocation)
                    }
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    if (key == getCurrentUserUid()) {
                        Log.i(TAG, "getNearlyLocations: onKeyMoved (Current User) : $key")
//                            currentLocation.postValue(location)
                    } else {
                        Log.i(TAG, "getNearlyLocations: onKeyMoved : $key")
                        nearlyLocation[key!!] = location!!
                        nearlyLocationLiveData.postValue(nearlyLocation)
                    }
                }

                override fun onKeyExited(key: String?) {
                    if (key == getCurrentUserUid()) {
                        Log.i(TAG, "getNearlyLocations: onKeyExited (Current User) : $key")
//                            currentLocation.postValue(location)
                    } else {
                        Log.i(TAG, "getNearlyLocations: onKeyExited : $key")
                        nearlyLocation.remove(key)
                        nearlyLocationLiveData.postValue(nearlyLocation)
                    }
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                    Log.i(TAG, "getNearlyLocations: onGeoQueryError")
                }

            })
    }

    fun addLocation(location: Location) {

        geoFire.setLocation(getCurrentUserUid(), GeoLocation(location.latitude, location.longitude))
    }

    fun addCurrentLocation() {
        currentLocation.value?.let {
            geoFire.setLocation(getCurrentUserUid(), GeoLocation(it.latitude, it.longitude))
        }
    }

    fun disableVisibility() {
        geoFire.removeLocation(getCurrentUserUid())
    }

    fun isUserDataExist(uid: String): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        cloudFirestoreDB.collection(Constants.User).document(uid)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.toObject(User::class.java) != null) {
                        result.value = Resource.success(true)
                    } else {
                        result.value = Resource.success(false)
                    }
                } else
                    result.value = Resource.error(it.exception!!.localizedMessage)
            }
        return result
    }

    fun getImage(url: String, destinationName: String): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()

        val imageReference = storageFirebase.reference.child(Constants.MEDIA).child(url)

        imageReference.getFile(File(destinationName))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    result.value = Resource.success(true)
                    deleteImage(imageReference)
                } else
                    result.value = Resource.error(it.exception!!.localizedMessage)
            }
        return result
    }

    private fun deleteImage(reference: StorageReference) {
        reference.delete()
        //todo handle remove complete listener
    }
}