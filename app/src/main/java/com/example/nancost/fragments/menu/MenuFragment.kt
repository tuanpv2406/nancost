package com.example.nancost.fragments.menu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.marginStart
import androidx.navigation.fragment.findNavController
import com.example.nancost.BuildConfig
import com.example.nancost.MainActivity
import com.example.nancost.R
import com.example.nancost.SplashActivity
import com.example.nancost.databinding.FragmentMenuBinding
import com.example.nancost.fragments.dialog.ActionDialog
import com.example.nancost.utils.AppConstant
import com.example.nancost.utils.SharedPreUtils
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MenuFragment : Fragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        binding.textVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.layoutTotal.setOnClickListener {
            findNavController().navigate(R.id.action_menuFragment_to_totalFragment)
        }
        binding.layoutChangePrice.setOnClickListener {
            showChangePriceDialog()
        }

        binding.layoutLogout.setOnClickListener {
            SharedPreUtils.putBoolean(AppConstant.Enum.HAS_LOGGED_IN, false)
            startActivity(Intent(requireContext(), SplashActivity::class.java))
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_menuFragment_to_listFragment)
            }
        })
    }

    private fun showChangePriceDialog() {
        ActionDialog.show( childFragmentManager,
            "Thay đổi giá cót",
            "Mời bạn nhập giá cót mới vào ô bên dưới",
            true
        ).apply {
            onNegativeActionListener = {
                dismiss()
            }
            onPositiveActionListener = {
                dismiss()
            }
        }
    }
}