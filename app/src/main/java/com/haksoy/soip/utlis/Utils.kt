package com.haksoy.soip.utlis

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.haksoy.soip.R
import java.util.*


/**
 * Helper functions to simplify permission checks/requests.
 */
private const val TAG = "SoIP:Utils"

class Utils {
    companion object {
        fun hasAndroidR(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        }

        fun hasAndroidQ(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }

        fun hasAndroidO(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

        fun hideWithAnimationY(view: View) {
            view.animate().translationY(view.height.toFloat()).alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
        }

        fun showWithAnimationY(view: View) {
            view.animate().translationY(0f).alpha(1.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                    }
                })
        }

        fun hideWithAnimationX(view: View) {
            view.animate().translationX(-view.width.toFloat()).alpha(0.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
//                view.visibility = View.GONE
                    }
                })
        }

        fun showWithAnimationX(view: View) {
            view.animate().translationX(0f).alpha(1.0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
//                    view.visibility = View.VISIBLE
                    }
                })
        }
    }
}

fun Context.hasPermission(permission: String): Boolean {

    // Background permissions didn't exit prior to Q, so it's approved by default.
    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
    ) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

fun Context.putPreferencesBoolean(key: String, value: Boolean) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    with(preferences.edit()) {
        putBoolean(key, value)
        commit()
    }
}

fun Context.getPreferencesBoolean(key: String, defaultValue: Boolean): Boolean {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    return preferences.getBoolean(key, defaultValue)
}

fun Context.putPreferencesString(key: String, value: String) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    with(preferences.edit()) {
        putString(key, value)
        commit()
    }
}

fun Context.getPreferencesString(key: String, defaultValue: String): String {
    val preferences = PreferenceManager.getDefaultSharedPreferences(this)
    return preferences.getString(key, defaultValue).toString()
}

fun Context.vibratePhone() {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Utils.hasAndroidO()) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(200)
    }
}

fun Context.isAppInForeground(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses ?: return false

    appProcesses.forEach { appProcess ->
        if (appProcess.importance ==
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
            appProcess.processName == packageName
        ) {
            return true
        }
    }
    return false
}

fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
    Log.i(TAG, "observeOnce  : called")
    observeForever(object : Observer<T> {
        override fun onChanged(value: T) {
            Log.i(TAG, "observeOnce  : onChanged")
            observer(value)
            removeObserver(this)
        }
    })
}

fun <T> LiveData<T>.observeWithProgress(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>
) {
//    ProgressHelper.getInstance().showLoading(context)//todo get obreserve  state start loading errors
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T) {
//            ProgressHelper.getInstance().hideLoading()
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun Context.startInstagram(username: String) {
    val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/$username"))
    i.setPackage("com.instagram.android")

    try {
        startActivity(i)
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://instagram.com/$username")
            )
        )
    }
}

fun Context.startTwitter(username: String) {
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("twitter://user?screen_name=$username")
            )
        )
    } catch (e: Exception) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/$username")
            )
        )
    }
}

fun Context.startFacebook(username: String) {
    val facebookUrl: String
    val FACEBOOK_URL = "https://www.facebook.com/$username"
    try {
        val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
        facebookUrl = if (versionCode >= 3002850) { //newer versions of fb app
            "fb://facewebmodal/f?href=$FACEBOOK_URL"
        } else { //older versions of fb app
            "fb://page/$username"
        }

        val facebookIntent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
        startActivity(facebookIntent)
    } catch (e: java.lang.Exception) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.facebook.com/$username")
            )
        )
    }

}

fun Fragment.requestPermissionWithRationale(
    permission: String,
    requestCode: Int,
    snackbar: Snackbar
) {
    val provideRationale = shouldShowRequestPermissionRationale(permission)

    if (provideRationale) {
        snackbar.show()
    } else {
        requestPermissions(arrayOf(permission), requestCode)
    }
}

fun Fragment.requestPermissionsWithRationale(
    permissions: Array<String>,
    requestCode: Int,
    snackbar: Array<Snackbar>
) {
    for (i in permissions.indices) {
        if (shouldShowRequestPermissionRationale(permissions[i])) {
            snackbar[i].show()
            return
        }
    }
    requestPermissions(permissions, requestCode)
}

fun Context.getCountryDialCode(): String? {
    val countryId: String?
    var countryDialCode: String? = null
    val telephonyMngr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    countryId = telephonyMngr.simCountryIso.uppercase(Locale.getDefault())
    val arrContryCode = resources.getStringArray(R.array.DialingCountryCode)
    for (i in arrContryCode.indices) {
        val arrDial = arrContryCode[i].split(",").toTypedArray()
        if (arrDial[1].trim { it <= ' ' } == countryId.trim()) {
            countryDialCode = arrDial[0]
            break
        }
    }
    return countryDialCode
}

fun Context.getCountryIso(): String {
    val telephonyMngr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telephonyMngr.simCountryIso.uppercase(Locale.getDefault())
}

fun Context.showMessage(message: String) {
    Log.i(TAG, "showMessage  : $message")
    AlertDialog.Builder(this, R.style.AlertDialogTheme)
        .setIcon(R.mipmap.ic_launcher)
        .setTitle(R.string.app_name)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(R.string.ok) { _, _ ->
        }.show()
}
