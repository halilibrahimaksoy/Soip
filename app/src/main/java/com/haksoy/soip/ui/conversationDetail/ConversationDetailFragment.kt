package com.haksoy.soip.ui.conversationDetail

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.inlineactivityresult.startActivityForResult
import com.bumptech.glide.Glide
import com.haksoy.soip.R
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.data.user.User
import com.haksoy.soip.databinding.FragmentConversationDetailBinding
import com.haksoy.soip.ui.CameraActivity
import com.haksoy.soip.ui.gallery.MediaGalleryFragment
import com.haksoy.soip.ui.main.SharedViewModel
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.IntentUtils
import com.haksoy.soip.utlis.NotificationHelper
import com.haksoy.soip.utlis.PermissionsUtil
import com.haksoy.soip.utlis.isConnected
import com.haksoy.soip.utlis.showMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ConversationDetailFragment @Inject constructor(private val notificationHelper: NotificationHelper) :
    Fragment(), View.OnClickListener,
    ConversationDetailAdapter.ConversationDetailItemClickListener {

    private val viewModel: ConversationDetailViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var binding: FragmentConversationDetailBinding
    private var adapter = ConversationDetailAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConversationDetailBinding.inflate(inflater, container, false)

        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        }
        (activity as AppCompatActivity).supportActionBar?.title = ""

        binding.inputLayout.btnSend.setOnClickListener(this)
        binding.inputLayout.btnStartCamera.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.imageView.setOnClickListener(this)
        binding.txtFullName.setOnClickListener(this)
        setupViewPager()
        fillUserData()
        focusToInput()


        binding.inputLayout.txtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().trim().isEmpty()) {
                    ImageViewCompat.setImageTintList(
                        binding.inputLayout.btnSend,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.icon_disabled
                            )
                        )
                    )
                    binding.inputLayout.btnSend.isEnabled = false
                } else if (requireContext().isConnected) {
                    ImageViewCompat.setImageTintList(
                        binding.inputLayout.btnSend,
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.icon_first
                            )
                        )
                    )
                    binding.inputLayout.btnSend.isEnabled = true
                }
            }

        })

        viewModel.connectionLiveData.observe(viewLifecycleOwner) { connected ->
            changeClickableForConnection(connected)
        }

        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)
        viewModel.markAsReadChat()

        return binding.root
    }

    private fun changeClickableForConnection(connected: Boolean) {
        if (connected) {

            ImageViewCompat.setImageTintList(
                binding.inputLayout.btnSend,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.icon_first))
            )
            binding.inputLayout.btnSend.isEnabled = true

            ImageViewCompat.setImageTintList(
                binding.inputLayout.btnStartCamera,
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.icon_first))
            )
            binding.inputLayout.btnStartCamera.isEnabled = true

        } else {
            ImageViewCompat.setImageTintList(
                binding.inputLayout.btnSend,
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.icon_disabled
                    )
                )
            )
            binding.inputLayout.btnSend.isEnabled = false
            ImageViewCompat.setImageTintList(
                binding.inputLayout.btnStartCamera,
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.icon_disabled
                    )
                )
            )
            binding.inputLayout.btnStartCamera.isEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.get(Constants.ConversationDetailFragmentSelectedUser)
            ?.let { viewModel.user = it as User }
    }

    override fun onResume() {
        super.onResume()
        notificationHelper.removeNotification(viewModel.user.uid)
    }

    private fun fillUserData() {
        if (viewModel.user.profileImage != null)
            showProfileImage(viewModel.user.profileImage!!)
        binding.txtFullName.text = viewModel.user.name

        viewModel.getConversationDetail(viewModel.user.uid)
        viewModel.conversationDetailList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            adapter.setItems(it as ArrayList<Chat>)
            binding.recyclerView.scrollToPosition(0)
        })
    }

    private fun showProfileImage(currentImageReferance: String) {
        Glide.with(binding.root)
            .load(currentImageReferance)
            .circleCrop()
            .into(binding.imageView)

    }

    private fun setupViewPager() {
        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = adapter
    }


    private fun removeForEveryOne(position: Int) {
        removeOnlyMe(position)
        viewModel.sendRemoveRequestAtPosition(position)
    }

    private fun removeOnlyMe(position: Int) {
        viewModel.removeChatAtPosition(position)
        adapter.removeAt(position)
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            if (PermissionsUtil.hasCameraPermissionGranted(context))
                startCamera()
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSend -> {
                sendChat()
            }

            R.id.btn_start_camera -> {
                if (PermissionsUtil.hasCameraPermissionGranted(context))
                    startCamera()
                else requestMultiplePermissions.launch(
                    PermissionsUtil.cameraPermission
                )
            }

            R.id.imageView,
            R.id.txtFullName -> {
                sharedViewModel.selectedUser.postValue(viewModel.user)
            }

            R.id.btnBack -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun sendChat() {
        viewModel.sendMessage(
            binding.inputLayout.txtMessage.text.toString().trim()
        )
        binding.inputLayout.txtMessage.setText("")
        focusToInput()
    }

    private fun focusToInput() {
        binding.inputLayout.txtMessage.requestFocus()
    }

    private fun startCamera() {
        val intent = Intent(activity, CameraActivity::class.java)
        intent.putExtra(IntentUtils.CAMERA_VIEW_SHOW_PICK_IMAGE_BUTTON, true)
        startActivityForResult(intent) { success, data ->
            val resultsIntent = data
            viewModel.sendMedia(
                resultsIntent!!.getStringExtra(IntentUtils.EXTRA_FILE_NAME_RESULT)!!,
                resultsIntent.getStringExtra(IntentUtils.EXTRA_PATH_RESULT)!!,
                (resultsIntent.getSerializableExtra(IntentUtils.EXTRA_TYPE_RESULT) as ChatType?)!!
            )
        }

    }

    override fun onClickChat(chat: Chat) {
        if (ChatType.isMedia(chat.type)) {
            val mediaGalleryFragment =
                MediaGalleryFragment.newInstance(
                    sharedViewModel.conversationDetailWithUser.value!!.uid,
                    chat.uid
                )
            childFragmentManager.beginTransaction()
                .replace(
                    R.id.galleryFragment,
                    mediaGalleryFragment,
                    Constants.MediaGalleryFragmentTag
                )
                .addToBackStack(Constants.MediaGalleryFragmentTag)
                .commit()
        }
    }

}