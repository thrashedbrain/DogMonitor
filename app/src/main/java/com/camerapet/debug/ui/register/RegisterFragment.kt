package com.camerapet.debug.ui.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.camerapet.debug.R
import com.camerapet.debug.data.wrappers.LoginResult
import com.camerapet.debug.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentRegisterBinding.inflate(inflater)
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                LoginResult.SUCCESS -> findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToMainFragment())
                LoginResult.ERROR -> Toast.makeText(requireContext(), "Register Error", Toast.LENGTH_SHORT).show()
            }
        }
        binding.registerBtn.setOnClickListener {
            viewModel.setData(binding.emailEdit.text.toString(), binding.passEdit.text.toString())
        }
        return binding.root
    }
}