package com.haksoy.soip.ui.discover

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.firebase.geofire.GeoLocation
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.user.User
import com.haksoy.soip.location.LocationRepository
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val TAG = "MapsViewModel"

@HiltViewModel
class MapsViewModel @Inject constructor(
    app: Application,
    val firebaseDao: FirebaseDao,
    private val locationRepository: LocationRepository
) : BaseViewModel(app) {


    val nearlyUsers = MutableLiveData<List<User>>()

    fun startObservingNearlyUsers() {
        Log.i(TAG, "startObservingNearlyUsers")
        firebaseDao.getLocation(firebaseDao.getCurrentUserUid())
        firebaseDao.currentLocation.observeForever(currentLocationObserver)
    }

    fun stopObservingNearlyUsers() {
        Log.i(TAG, "stopObservingNearlyUsers")
        firebaseDao.currentLocation.removeObserver(currentLocationObserver)
        firebaseDao.nearlyUser.removeObserver(nearlyUserObserver)
        firebaseDao.stopNearlyLocationObserve()
    }

    private val currentLocationObserver = Observer<GeoLocation> {
        Log.i(TAG, "fetchNearlyUsers: Location Observed")
        firebaseDao.startNearlyLocationObserve(it)
        firebaseDao.nearlyUser.observeForever(nearlyUserObserver)
    }
    private val nearlyUserObserver = Observer<Resource<List<User>>> { it ->
        if (it.status == Resource.Status.SUCCESS) {
            Log.i(TAG, "fetchNearlyUsers: postValue data")
            nearlyUsers.postValue(it.data!!)
        } else if (it.status == Resource.Status.ERROR) {
            errorMessages.postValue(it.message)
        }
    }

    fun disableVisibility() = firebaseDao.disableVisibility()

    fun addLocation() = firebaseDao.addCurrentLocation()

    fun startLocationUpdates() = locationRepository.startLocationUpdates()

    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()
}