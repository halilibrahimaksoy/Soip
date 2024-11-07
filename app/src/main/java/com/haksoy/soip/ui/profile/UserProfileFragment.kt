package com.haksoy.soip.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.haksoy.soip.R
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.FragmentUserProfileBinding
import com.haksoy.soip.ui.main.MainActivity
import com.haksoy.soip.ui.main.SharedViewModel
import com.haksoy.soip.ui.settings.SettingsActivity
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.utlis.Utils
import com.haksoy.soip.utlis.observeWithProgress
import com.haksoy.soip.utlis.showMessage
import com.haksoy.soip.utlis.startFacebook
import com.haksoy.soip.utlis.startInstagram
import com.haksoy.soip.utlis.startTwitter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class UserProfileFragment @Inject constructor() : Fragment(), View.OnClickListener {


    enum class Status {
        REGISTRATION,
        AUTH_USER,
        OTHER_USER
    }

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var _user: User

    @Inject
    lateinit var firebaseDao: FirebaseDao


    private val viewModel: UserProfileViewModel by viewModels()


    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var editMode: Boolean = false
    private var newImageUri: Uri? = null
    private lateinit var reasonStatus: Status
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(layoutInflater, container, false)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        }
        (activity as AppCompatActivity).supportActionBar?.title = ""


        binding.imageView.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.btnEdit.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.btnSend.setOnClickListener(this)
        binding.btnSettings.setOnClickListener(this)
        binding.imgInstagram.setOnClickListener(this)
        binding.imgFacebook.setOnClickListener(this)
        binding.imgTwitter.setOnClickListener(this)

        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)

        viewModel.connectionLiveData.observe(viewLifecycleOwner, { connected ->
            if (reasonStatus == Status.AUTH_USER) {
                binding.btnEdit.isEnabled = connected
            }
        })
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.get(Constants.UserProfileFragmentReason)?.let {
            reasonStatus = it as Status
        }
        arguments?.get(Constants.UserProfileFragmentSelectedUser)?.let {
            viewModel.currentUser.value = (it as User)
        }

        if (reasonStatus == Status.REGISTRATION)
            editMode = true

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        when (reasonStatus) {
            Status.AUTH_USER -> {
                binding.txtPhoneNumber.text = viewModel.getPhoneNumber()
                viewModel.fetchUserDataFromLocale(viewModel.getUid())
            }

            Status.REGISTRATION -> {
                binding.txtPhoneNumber.text = viewModel.getPhoneNumber()
                viewModel.currentUser.value = User(viewModel.getUid(), viewModel.getPhoneNumber())
                _user = User(
                    viewModel.getUid(),
                    viewModel.getPhoneNumber(),
                    createDate = System.currentTimeMillis()
                )
            }

            Status.OTHER_USER -> {
                viewModel.fetchUserData()
            }
        }

        viewModel.currentUser.observe(viewLifecycleOwner, Observer {
            fillUserData(it)
            _user = it
        })
        optimizeMenuForStatus()
        setEditMode()
    }

    private fun fillUserData(user: User) {
        if (user.profileImage != null)
            showProfileImage(user.profileImage!!)
        if (reasonStatus != Status.OTHER_USER && user.phoneNumber != null)
            binding.txtPhoneNumber.text = user.phoneNumber
        binding.txtFullName.text = user.name
        binding.txtInfo.text = user.info
        if (!user.socialMedia.instagram.isNullOrEmpty())
            binding.imgInstagram.visibility = View.VISIBLE

        if (!user.socialMedia.facebook.isNullOrEmpty())
            binding.imgFacebook.visibility = View.VISIBLE

        if (!user.socialMedia.twitter.isNullOrEmpty())
            binding.imgTwitter.visibility = View.VISIBLE
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data!!
                // Use the uri to load the image
                // Only if you are not using crop feature:
                uri.let { galleryUri ->
                    activity?.contentResolver?.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                //////////////
            }
        }

    private val profileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                newImageUri = it.data?.data!!
                showProfileImage(newImageUri.toString())
                _user.profileImage = newImageUri.toString()
            } else if (it.resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(context, ImagePicker.getError(it.data), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    private fun pickImage() {
        ImagePicker.with(requireActivity())
            .crop()
            .cropOval()
            .maxResultSize(512, 512, true)
            .provider(ImageProvider.BOTH) // Or bothCameraGallery()
            .setDismissListener {
                Log.i("ImagePicker", "onDismiss")
            }
            .createIntentFromDialog { profileLauncher.launch(it) }
    }

    private fun validateForm(): Boolean {
        var valid = true

        val fullName2 = binding.txtFullName2.text.toString()
        if (TextUtils.isEmpty(fullName2)) {
            binding.txtFullName2.error = "Required."
            valid = false
        } else {
            binding.txtFullName2.error = null
        }


        return valid
    }

    private fun updateUserProfile() {
        if (validateForm()) {
            viewModel.updateUserProfile(_user)
                .observeWithProgress(requireContext(), viewLifecycleOwner, Observer {
                    if (it.status == Resource.Status.SUCCESS) {
                        updateUserProfileCompleted()
                    } else if (it.status == Resource.Status.ERROR) {
                        Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun showProfileImage(currentImageReferance: String) {
        Glide.with(binding.root)
            .load(currentImageReferance)
            .circleCrop()
            .into(binding.imageView)

    }

    private fun setEditMode() {
        if (editMode) {
            if (reasonStatus == Status.AUTH_USER)
                binding.btnCancel.visibility = View.VISIBLE
            binding.btnSave.visibility = View.VISIBLE
            binding.btnEdit.visibility = View.GONE
            binding.btnSettings.visibility = View.GONE
            binding.imageView.isClickable = true
            Utils.hideWithAnimationX(binding.lnrShow)
            Utils.showWithAnimationX(binding.lnrEdit)

            fillEditFields()
        } else {
            binding.btnSave.visibility = View.GONE
            binding.btnCancel.visibility = View.GONE
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnSettings.visibility = View.VISIBLE
            binding.imageView.isClickable = false
            Utils.hideWithAnimationX(binding.lnrEdit)
            Utils.showWithAnimationX(binding.lnrShow)
        }
    }


    private fun optimizeMenuForStatus() {
        when (reasonStatus) {
            Status.REGISTRATION -> {
                binding.btnCancel.visibility = View.GONE
                binding.rltvEdit.visibility = View.VISIBLE
                binding.rltvPresent.visibility = View.GONE
            }

            Status.OTHER_USER -> {
                binding.rltvEdit.visibility = View.GONE
                binding.rltvPresent.visibility = View.VISIBLE
            }

            Status.AUTH_USER -> {
                binding.rltvEdit.visibility = View.VISIBLE
                binding.rltvPresent.visibility = View.GONE
            }
        }
    }

    private fun fillEditFields() {
        binding.txtFullName2.setText(binding.txtFullName.text)
        binding.txtInfo2.setText(binding.txtInfo.text)

        _user.let {
            binding.txtInstagram2.setText(it.socialMedia.instagram)
            binding.txtFacebook2.setText(it.socialMedia.facebook)
            binding.txtTwitter2.setText(it.socialMedia.twitter)
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnEdit -> {
                editMode = true
                setEditMode()
                sharedViewModel.userProfileEditMode.postValue(true)
            }

            R.id.btnSave -> {
                setNewUserData()
                updateUserProfile()
            }

            R.id.btnCancel -> {
                editMode = false
                setEditMode()
                sharedViewModel.userProfileEditMode.postValue(false)
            }

            R.id.btnBack -> {
                activity?.onBackPressed()
            }

            R.id.btnSettings -> {
                activity?.startActivity(Intent(context, SettingsActivity::class.java))
            }

            R.id.btnSend -> {
                sharedViewModel.conversationDetailWithUser.postValue(_user)
            }

            R.id.imageView -> {
                if (editMode)
                    pickImage()
            }

            R.id.imgInstagram -> {
                _user.socialMedia.instagram?.let { activity?.startInstagram(it) }
            }

            R.id.imgTwitter -> {
                _user.socialMedia.twitter?.let { activity?.startTwitter(it) }
            }

            R.id.imgFacebook -> {
                _user.socialMedia.facebook?.let { activity?.startFacebook(it) }
            }
        }
    }


    private fun updateUserProfileCompleted() {
        editMode = false
        setEditMode()
        viewModel.fetchUserData()
        sharedViewModel.userProfileEditMode.postValue(false)
        if (reasonStatus == Status.REGISTRATION) {
            activity?.startActivity(Intent(context, MainActivity::class.java))
            activity?.finish()
        }
    }

    private fun setNewUserData() {
        _user.name = binding.txtFullName2.text.toString()
        _user.info = binding.txtInfo2.text.toString()
        _user.socialMedia.instagram = binding.txtInstagram2.text.toString()
        _user.socialMedia.twitter = binding.txtTwitter2.text.toString()
        _user.socialMedia.facebook = binding.txtFacebook2.text.toString()
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            _user.token = it.result
        }
    }
}