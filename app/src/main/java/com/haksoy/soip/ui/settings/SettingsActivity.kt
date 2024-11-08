package com.haksoy.soip.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.Snackbar
import com.haksoy.soip.BuildConfig
import com.haksoy.soip.R
import com.haksoy.soip.databinding.SettingsActivityBinding
import com.haksoy.soip.ui.splash.SplashActivity
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.hasPermission
import com.haksoy.soip.utlis.putPreferencesBoolean
import com.haksoy.soip.utlis.requestPermissionWithRationale
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "SettingsActivity"

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    lateinit var binding: SettingsActivityBinding

    @Inject
    lateinit var viewModel: SettingsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setTitle(R.string.title_activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnLogout.setOnClickListener {
            viewModel.signOut()
            finish()
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val backgroundRationalSnackbar by lazy {
                Snackbar.make(
                    view,
                    R.string.background_location_permission_rationale,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.ok) {
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            Constants.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
                        )
                    }
            }
            val enable_background_location =
                findPreference<SwitchPreferenceCompat>(Constants.enable_background_location_key)
            enable_background_location?.let {
                it.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        if (!activity?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!!) {
                            requestPermissionWithRationale(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                Constants.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                                backgroundRationalSnackbar
                            )
                            false
                        } else
                            true
                    }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")
        if (permissions[0] == android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                putPreferencesBoolean(Constants.enable_background_location_key, true)
                (this.supportFragmentManager.fragments[0] as SettingsFragment).let {
                    it.preferenceScreen.removeAll()
                    it.addPreferencesFromResource(R.xml.root_preferences)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.background_permission_denied_explanation),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID,
                            null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    .show()
            }
        }
    }

}