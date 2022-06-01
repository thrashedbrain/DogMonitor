package com.camerapet.debug.ui.owner

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.camerapet.debug.R
import com.camerapet.debug.databinding.FragmentOwnerBinding
import com.camerapet.debug.rtc.*
import dagger.hilt.android.AndroidEntryPoint
import org.webrtc.*

@AndroidEntryPoint
class OwnerFragment : Fragment() {

    private var binding: FragmentOwnerBinding? = null
    private val viewModel: OwnerViewModel by viewModels()

    private var rtcClient: RTCClient? = null
    private var signallingClient: SignalingClient? = null

    private val audioManager by lazy { RTCAudioManager.create(requireContext()) }

    private val TAG = "OwnerFragment"

    private var isJoin = true
    private var isMute = false
    private var isVideoPaused = false
    private var inSpeakerMode = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOwnerBinding.inflate(inflater)
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        audioManager.setMicrophoneMute(true)
        binding?.commandBtn?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> audioManager.setMicrophoneMute(false)
                MotionEvent.ACTION_UP -> audioManager.setMicrophoneMute(true)
            }
            false
        }
        //TODO add doze check
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                activity?.startForegroundService(Intent(requireContext(), OwnerService::class.java))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                activity?.startForegroundService(Intent(requireContext(), OwnerService::class.java))
            }
            else -> {
                activity?.startService(Intent(requireContext(), OwnerService::class.java))
            }
        }

        binding?.closeImg?.setOnClickListener {
            viewModel.updateState()
            activity?.stopService(Intent(requireContext(), OwnerService::class.java))
            findNavController().popBackStack()
        }

        viewModel.tags.observe(viewLifecycleOwner) { meetId ->
            binding?.remoteView?.release()
            signallingClient?.clearTag()
            rtcClient = RTCClient(
                activity?.application!!,
                object : PeerConnectionObserver() {
                    override fun onIceCandidate(p0: IceCandidate?) {
                        super.onIceCandidate(p0)
                        signallingClient?.sendIceCandidate(p0, isJoin)
                        rtcClient?.addIceCandidate(p0)
                    }

                    override fun onAddStream(p0: MediaStream?) {
                        super.onAddStream(p0)
                        viewModel.updateWatchingState()
                        p0?.videoTracks?.get(0)?.addSink(binding?.remoteView)
                    }

                    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
                        Log.e(TAG, "onIceConnectionChange: $p0")
                    }

                    override fun onIceConnectionReceivingChange(p0: Boolean) {
                        Log.e(TAG, "onIceConnectionReceivingChange: $p0")
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                        Log.e(TAG, "onConnectionChange: $newState")
                    }

                    override fun onDataChannel(p0: DataChannel?) {
                        Log.e(TAG, "onDataChannel: $p0")
                    }

                    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                        Log.e(TAG, "onStandardizedIceConnectionChange: $newState")
                    }

                    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
                        Log.e(TAG, "onAddTrack: $p0 \n $p1")
                    }

                    override fun onTrack(transceiver: RtpTransceiver?) {
                        Log.e(TAG, "onTrack: $transceiver" )
                    }
                }
            )

            binding?.remoteView?.let { rtcClient?.initSurfaceView(it) }
            rtcClient?.startLocalAudioCapture()
            signallingClient =  SignalingClient(meetId, object : SignalingClientListener {
                override fun onConnectionEstablished() {
                }

                override fun onOfferReceived(description: SessionDescription) {
                    rtcClient?.onRemoteSessionReceived(description)
                    Constants.isIntiatedNow = false
                    rtcClient?.answer(sdpObserver,meetId)
                }

                override fun onAnswerReceived(description: SessionDescription) {
                    Log.d("asdasdasd", "asdasd")
                    rtcClient?.onRemoteSessionReceived(description)
                    Constants.isIntiatedNow = false
                }

                override fun onIceCandidateReceived(iceCandidate: IceCandidate) {
                    rtcClient?.addIceCandidate(iceCandidate)
                }

                override fun onReAnswerReceived(description: SessionDescription) {

                }

                override fun onCallEnded() {
                    if (!Constants.isCallEnded) {
                        Constants.isCallEnded = true
                        rtcClient?.endCall(meetId)
                        findNavController().popBackStack()
                    }
                }
            })

            rtcClient?.enableAudio(true)
        }
        return binding?.root
    }

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
//            signallingClient.send(p0)
        }
    }

    override fun onDestroyView() {
        //TODO Fix cancellableCoroutine
        viewModel.updateState()
        binding?.remoteView?.let { rtcClient?.releaseSurfaceView(it) }
        binding?.remoteView?.release()
        rtcClient?.releaseCapturer()
        rtcClient?.closeConnection()
        rtcClient = null
        signallingClient?.destroy()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}