package com.haksoy.soip.ui.discover

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.haksoy.soip.BuildConfig
import com.haksoy.soip.R
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.FragmentMapsBinding
import com.haksoy.soip.ui.main.SharedViewModel
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.PermissionsUtil
import com.haksoy.soip.utlis.Utils
import com.haksoy.soip.utlis.getPreferencesBoolean
import com.haksoy.soip.utlis.hasPermission
import com.haksoy.soip.utlis.putPreferencesBoolean
import com.haksoy.soip.utlis.requestPermissionWithRationale
import com.haksoy.soip.utlis.requestPermissionsWithRationale
import com.haksoy.soip.utlis.showMessage
import dagger.hilt.android.AndroidEntryPoint
import io.ghyeok.stickyswitch.widget.StickySwitch
import org.jetbrains.annotations.NotNull
import javax.inject.Inject
import kotlin.math.min


private const val TAG = "MapsFragment"

@AndroidEntryPoint
class MapsFragment @Inject constructor() : Fragment() {


    private lateinit var binding: FragmentMapsBinding
    lateinit var mapFragment: SupportMapFragment
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val viewModel: MapsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        binding.swcPower.onSelectedChangeListener =
            (object : StickySwitch.OnSelectedChangeListener {
                override fun onSelectedChange(
                    @NotNull direction: StickySwitch.Direction,
                    @NotNull text: String
                ) {
                    Log.d(TAG, "Now Selected : " + direction.name + ", Current Text : " + text)
                    when (direction) {
                        StickySwitch.Direction.LEFT -> {
                            enableVisibility()
                            activity?.putPreferencesBoolean(Constants.map_visibility_key, true)
                        }

                        else -> {
                            disableVisibility()
                            activity?.putPreferencesBoolean(Constants.map_visibility_key, false)
                        }
                    }
                }
            })

        viewModel.connectionLiveData.observe(viewLifecycleOwner, Observer { connected ->
            binding.txtConMessage.visibility = if (connected) View.GONE else View.VISIBLE
        })
        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)
        return binding.root
    }

    private fun enableVisibility() {
        Log.i(TAG, "enableVisibility")
        binding.swcPower.setLeftIcon(R.drawable.ic_visibility_filled)
        binding.swcPower.setRightIcon(R.drawable.ic_visibility_off_outline)
        mapFragment.getMapAsync { googleMap ->
            googleMap.uiSettings.setAllGesturesEnabled(true)
            googleMap.setOnMarkerClickListener(markerClickListener)
        }
        startNearlyUser()
    }

    private fun disableVisibility() {
        Log.i(TAG, "disableVisibility")
        binding.swcPower.setLeftIcon(R.drawable.ic_visibility_outline)
        binding.swcPower.setRightIcon(R.drawable.ic_visibility_off_filled)
        mapFragment.getMapAsync { googleMap ->
            googleMap.uiSettings.setAllGesturesEnabled(false)
            googleMap.setOnMarkerClickListener {
                true
            }
        }
        stopNearlyUser()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!

        if (activity?.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)!!) {
            prepareUi()
        } else {
            getPermissionRequest()
        }

    }

    private fun prepareUi() {
        mapFragment.getMapAsync { googleMap ->
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            binding.myLocation.setOnClickListener { view ->
                moveToCurrentLocation(googleMap)
            }
            moveToCurrentLocation(googleMap)
            googleMap.setMaxZoomPreference(14f)
        }

        if (activity?.getPreferencesBoolean(Constants.map_visibility_key, true)!!) {
            binding.swcPower.setDirection(StickySwitch.Direction.LEFT)
            enableVisibility()
        } else {
            disableVisibility()
            binding.swcPower.setDirection(StickySwitch.Direction.RIGHT)
        }
    }

    private val nearlyUserObserver = Observer<List<User>> { userList ->
        Log.i(TAG, "updateMap  :  nearlyUsers observed")
        mapFragment.getMapAsync { googleMap ->
            googleMap.clear()
            for (user in userList) {
                Glide.with(activity?.applicationContext!!)
                    .asBitmap()
                    .circleCrop()
                    .load(user.profileImage)
                    .apply(RequestOptions().override(100, 100))
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            googleMap.addMarker(getMarkerOptions(user))?.apply {
                                this.tag = user.uid
                            }?.setIcon(
                                BitmapDescriptorFactory.fromBitmap(
                                    addBorder(
                                        resource,
                                        binding.root.context
                                    )
                                )
                            )
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            googleMap.addMarker(getMarkerOptions(user))?.apply {
                                this.tag = user.uid
                            }
                                ?.setIcon(BitmapDescriptorFactory.fromResource(com.haksoy.soip.R.mipmap.ic_profile))
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            TODO("Not yet implemented")
                        }
                    })
            }
        }
//                 ProgressHelper.getInstance().hideLoading()
        Log.i(TAG, "updateMap  :  nearlyUsers added to maps")
    }

    private val markerClickListener = GoogleMap.OnMarkerClickListener { marker ->
        showUserList(marker.tag.toString())
        true

    }

    private fun getPermissionRequest() {
        AlertDialog.Builder(activity, R.style.AlertDialogTheme)
            .setIcon(R.mipmap.ic_launcher)
            .setTitle(R.string.app_name)
            .setMessage(R.string.fine_location_permission_dialog)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                requestPermissionsWithRationale(
                    PermissionsUtil.fineLocationPermission,
                    Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                    getPermissionSnackbar()
                )
            }.show()

    }

    private fun getBackgroundPermission() {
        if (!activity?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!!)
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Constants.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE,
                backgroundRationalSnackbar
            )
    }

    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            binding.root,
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.ok) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
    }

    private val backgroundRationalSnackbar by lazy {
        Snackbar.make(
            binding.root,
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


    private fun getPermissionSnackbar() =
        if (Utils.hasAndroidR()) arrayOf(fineLocationRationalSnackbar) else arrayOf(
            fineLocationRationalSnackbar,
            backgroundRationalSnackbar
        )


    private fun addBorder(resource: Bitmap, context: Context): Bitmap {
        val w = resource.width
        val h = resource.height
        val radius = min(h / 2, w / 2)
        val output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888)
        val p = Paint()
        p.isAntiAlias = true
        val c = Canvas(output)
        c.drawARGB(0, 0, 0, 0)
        p.style = Paint.Style.FILL
        c.drawCircle((w / 2 + 4).toFloat(), (h / 2 + 4).toFloat(), radius.toFloat(), p)
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        c.drawBitmap(resource, 4f, 4f, p)
        p.xfermode = null
        p.style = Paint.Style.STROKE
        p.color = ContextCompat.getColor(context, R.color.profile_imege_border_color)
        p.strokeWidth = 3f
        c.drawCircle((w / 2 + 4).toFloat(), (h / 2 + 4).toFloat(), radius.toFloat(), p)
        return output
    }

    private fun moveToCurrentLocation(it: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation.addOnSuccessListener { location ->
            if (location != null)
                it.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 14f
                    )
                )
        }

    }

    private fun showUserList(selectedUserUid: String) {
        Log.i(TAG, "showUserList  :  selectedUserList added new value")
        sharedViewModel.selectedUserList = viewModel.nearlyUsers.value as ArrayList<User>
        Log.i(TAG, "showUserList  :  selectedUserUid added new value")
        sharedViewModel.selectedUserUid.postValue(selectedUserUid)
    }

    private fun getMarkerOptions(user: User): MarkerOptions {
        return MarkerOptions().position(
            LatLng(
                user.location.latitude,
                user.location.longitude
            )
        ).title(user.name)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        Log.d(TAG, "onRequestPermissionResult")
        val permissionDeniedExplanation =
            if (requestCode == Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
                R.string.fine_permission_denied_explanation
            } else {
                R.string.background_permission_denied_explanation
            }

        val settingsSnackbar = Snackbar.make(
            binding.root,
            permissionDeniedExplanation,
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
        if (requestCode == Constants.REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prepareUi()
                getBackgroundPermission()
            } else {
                settingsSnackbar.show()
            }

        } else if (requestCode == Constants.REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                settingsSnackbar.show()

        }
        if (activity?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!!)
            context?.putPreferencesBoolean(
                Constants.enable_background_location_key,
                true
            )

    }

    private fun startNearlyUser() {
        Log.i(TAG, "startNearlyUser")
        viewModel.startLocationUpdates()
        viewModel.addLocation()
        viewModel.startObservingNearlyUsers()
        viewModel.nearlyUsers.observe(viewLifecycleOwner, nearlyUserObserver)

    }

    private fun stopNearlyUser() {
        Log.i(TAG, "stopNearlyUser")
        viewModel.stopObservingNearlyUsers()
        viewModel.nearlyUsers.removeObserver(nearlyUserObserver)
        viewModel.disableVisibility()
    }


    override fun onStop() {
        super.onStop()
        if (!context?.getPreferencesBoolean(
                Constants.enable_background_location_key,
                false
            )!!
        ) {
            viewModel.stopLocationUpdates()
        }
        viewModel.stopObservingNearlyUsers()
    }

}