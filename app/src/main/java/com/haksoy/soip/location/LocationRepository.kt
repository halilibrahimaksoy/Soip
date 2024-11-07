package com.haksoy.soip.location

import android.location.Location
import android.util.Log
import androidx.annotation.MainThread
import com.haksoy.soip.MainApplication
import com.haksoy.soip.data.FirebaseDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

private const val TAG = "SoIP:LocationRepository"

/**
 * Access point for database (MyLocation data) and location APIs (start/stop location updates and
 * checking location update status).
 */
class LocationRepository @Inject constructor(
    private val firebaseDao: FirebaseDao,
    private val myLocationManager: LocationManager
) {

    /**
     * Adds list of locations to the database.
     */
    fun addLocation(myLocationEntities: Location) {
        Log.i(TAG, "addLocation  :  new location added")
        firebaseDao.addLocation(myLocationEntities)

    }

    /**
     * Subscribes to location updates.
     */
    @MainThread
    fun startLocationUpdates() = myLocationManager.startLocationUpdates()

    /**
     * Un-subscribes from location updates.
     */
    @MainThread
    fun stopLocationUpdates() = myLocationManager.stopLocationUpdates()

}
