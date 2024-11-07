package com.haksoy.soip.utlis

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionsUtil {
    const val REQUEST_CAMERA_PERMISSION = 107

    val cameraPermission = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )

    val fineLocationPermission =
        if (Utils.hasAndroidR()) arrayOf(Manifest.permission.ACCESS_FINE_LOCATION) else arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

    fun hasCameraPermissionGranted(context: Context?): Boolean {
        return hasGrantedPermissions(context!!, cameraPermission)
    }

    private fun hasGrantedPermissions(context: Context, permissionList: Array<String>): Boolean {
        for (permission in permissionList) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun permissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }
}