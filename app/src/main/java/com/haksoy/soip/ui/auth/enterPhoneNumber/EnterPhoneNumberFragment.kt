package com.haksoy.soip.ui.auth.enterPhoneNumber

import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.haksoy.soip.R
import com.haksoy.soip.databinding.FragmentEnterPhoneNumberBinding
import com.haksoy.soip.ui.auth.AuthenticationViewModel
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.ProgressHelper
import com.haksoy.soip.utlis.getCountryDialCode
import com.haksoy.soip.utlis.showMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class EnterPhoneNumberFragment : Fragment() {
    lateinit var binding: FragmentEnterPhoneNumberBinding

    private val viewModel: AuthenticationViewModel by viewModels()
    @Inject
    lateinit var progressHelper: ProgressHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEnterPhoneNumberBinding.inflate(inflater, container, false)


        binding.countryCodeText.setText(requireContext().getCountryDialCode())
        binding.countryCodeText.hint = requireContext().getCountryDialCode()

        binding.countryCodeText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!s.toString().startsWith("+")) {
                    binding.countryCodeText.setText("+")
                    Selection.setSelection(
                        binding.countryCodeText.text,
                        binding.countryCodeText.text.length
                    )

                }
            }

        })


        binding.countryCodeText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == KeyEvent.KEYCODE_CALL || actionId == KeyEvent.KEYCODE_ENDCALL)
                binding.phoneNumberText.requestFocus()
            true
        }
        binding.phoneNumberText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == KeyEvent.KEYCODE_CALL || actionId == KeyEvent.KEYCODE_ENDCALL)
                binding.generateBtn.performClick()
            true
        }
        binding.generateBtn.setOnClickListener {
            val country_code: String = binding.countryCodeText.text.toString()
            val phone_number: String = binding.phoneNumberText.text.toString()
            val complete_phone_number =
                "$country_code$phone_number"
            progressHelper.showLoading(requireContext())
            viewModel.verifyPhoneNumber(requireActivity(), complete_phone_number)
        }


        viewModel.verificationId.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(
                R.id.action_enterPhoneNumberFragment_to_otpValidationFragment,
                Bundle().apply {
                    putString(Constants.VerificationId, it)
                })
        })

        viewModel.connectionLiveData.observe(viewLifecycleOwner, Observer { connected ->
            changeClickableForConnection(connected)
        })

        viewModel.errorMessages.observe(viewLifecycleOwner, requireContext()::showMessage)

        return binding.root
    }

    private fun changeClickableForConnection(connected: Boolean) {
        if (connected) {
            binding.generateBtn.setBackgroundColor(resources.getColor(R.color.icon_first))
            binding.generateBtn.isEnabled = true

        } else {
            binding.generateBtn.setBackgroundColor(resources.getColor(R.color.icon_disabled))
            binding.generateBtn.isEnabled = false
        }
    }
}