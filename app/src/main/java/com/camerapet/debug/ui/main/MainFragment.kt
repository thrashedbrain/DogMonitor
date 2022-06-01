package com.camerapet.debug.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.camerapet.debug.R
import com.camerapet.debug.data.common.PowerSaverHandler
import com.camerapet.debug.data.repository.UserRepository
import com.camerapet.debug.databinding.FragmentMainBinding
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        val powerSaverHandler = PowerSaverHandler(requireContext())
        binding.dogBtn.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToPetFragment())
        }
        binding.ownerBtn.setOnClickListener {
            when (powerSaverHandler.check()) {
                PowerSaverHandler.DozeState.UNSUPPORTED ->
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToOwnerFragment())
                PowerSaverHandler.DozeState.IGNORED -> {
                    //TODO make alert to explain battery optimisations need to user
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        powerSaverHandler.startBatteryOptimizationIntent()
                    }
                }
                PowerSaverHandler.DozeState.CHECKED -> {
                    findNavController().navigate(MainFragmentDirections.actionMainFragmentToOwnerFragment())
                }
            }
        }

        lifecycleScope.launch {
            //Todo check this phone is watcher
            if (userRepository.getState() == "watching") {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToOwnerFragment())
            }
        }

        return binding.root
    }
}