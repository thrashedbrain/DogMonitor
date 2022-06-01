package com.camerapet.debug.ui.login

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
import com.camerapet.debug.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLoginBinding.inflate(inflater)
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                LoginResult.SUCCESS -> findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToMainFragment())
                LoginResult.ERROR -> Toast.makeText(requireContext(), "Auth Error", Toast.LENGTH_SHORT).show()
            }
        }
        binding.loginBtn.setOnClickListener {
            viewModel.login(binding.emailEdit.text.toString(), binding.passEdit.text.toString())
        }
        binding.registerBtn.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
        return binding.root
    }
}