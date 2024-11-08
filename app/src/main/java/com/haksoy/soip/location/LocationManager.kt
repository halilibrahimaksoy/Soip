package com.haksoy.soip.location

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.MainThread
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.haksoy.soip.utlis.hasPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "SoIP:LocationManager"

/**
 * Manages all location related tasks for the app.
 */
class LocationManager @Inject constructor(@ApplicationContext val context: Context) {

    /**
     * Uses the FusedLocationProvider to start location updates if the correct fine locations are
     * approved.
     *
     * @throws SecurityException if ACCESS_FINE_LOCATION permission is removed before the
     * FusedLocationClient's requestLocationUpdates() has been completed.
     */
    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")

        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return
        val workRequest = PeriodicWorkRequestBuilder<BgLocationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED) // Optional: adjust based on your needs
                    .setRequiresBatteryNotLow(true) // Optional: avoid running when battery is low
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                BgLocationWorker.workName,  // Unique work name to avoid duplicates
                ExistingPeriodicWorkPolicy.REPLACE,  // Replace any existing work with the same name
                workRequest
            )
    }

    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        WorkManager.getInstance(context).cancelUniqueWork(BgLocationWorker.workName)

    }
}
