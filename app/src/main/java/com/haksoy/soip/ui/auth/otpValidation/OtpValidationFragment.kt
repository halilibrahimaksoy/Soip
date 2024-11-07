package com.haksoy.soip.ui.auth.otpValidation

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.haksoy.soip.R
import com.haksoy.soip.databinding.FragmentOtpValidationBinding
import com.haksoy.soip.ui.auth.AuthenticationViewModel
import com.haksoy.soip.utlis.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OtpValidationFragment : Fragment() {
    private lateinit var binding: FragmentOtpValidationBinding
    private val viewModel: AuthenticationViewModel by viewModels()

    lateinit var verificationId: String
    @Inject
    lateinit var progressHelper: ProgressHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString(Constants.VerificationId)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtpValidationBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        binding.otpTextView.setOnEditorActionListener { v, actionId, event ->
            if (actionId == KeyEvent.KEYCODE_CALL || actionId == KeyEvent.KEYCODE_ENDCALL)
                binding.verifyBtn.performClick()
            true
        }
        binding.verifyBtn.setOnClickListener {
            viewModel.signInWithPhoneAuthCredential(
                verificationId,
                binding.otpTextView.text.toString()
            )
                .observeWithProgress(
                    requireContext(), viewLifecycleOwner,
                    Observer {
                        progressHelper.hideLoading()
                        if (it.status == Resource.Status.SUCCESS) {
                            findNavController().navigate(R.id.action_otpValidationFragment_to_mainActivity)
                            activity?.putPreferencesString(Constants.USER_UID, it.data!!)
                            activity?.finish()
                        } else if (it.status == Resource.Status.ERROR) {
                            viewModel.errorMessages.postValue(it.message)
                        }
                    })
        }

        viewModel.connectionLiveData.observe(viewLifecycleOwner, Observer { connected ->
            changeClickableForConnection(connected)
        })

        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)

        return binding.root
    }

    private fun changeClickableForConnection(connected: Boolean) {
        if (connected) {
            binding.verifyBtn.setBackgroundColor(resources.getColor(R.color.icon_first))
            binding.verifyBtn.isEnabled = true

        } else {
            binding.verifyBtn.setBackgroundColor(resources.getColor(R.color.icon_disabled))
            binding.verifyBtn.isEnabled = false
        }
    }

}