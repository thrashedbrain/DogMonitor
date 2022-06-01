package com.camerapet.debug.ui.pet

import android.content.Context
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.camerapet.debug.R
import com.camerapet.debug.data.common.Timer
import com.camerapet.debug.databinding.FragmentPetBinding
import com.camerapet.debug.rtc.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.webrtc.*
import java.io.File

@AndroidEntryPoint
class PetFragment : Fragment() {

    private val viewModel: PetViewModel by viewModels()

    private var rtcClient: RTCClient? = null
    private var signallingClient: SignalingClient? = null

    private val audioManager by lazy { RTCAudioManager.create(requireContext()) }
    private val TAG = "PetFragment"

    private var isJoin = false
    private var isMute = false
    private var isVideoPaused = false
    private var inSpeakerMode = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPetBinding.inflate(inflater)
        audioManager.selectAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)
        viewModel.tags.observe(viewLifecycleOwner) { meetId ->
            rtcClient?.releaseCapturer()
            signallingClient?.clearTag()
            binding.remoteView.release()
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
                        Log.e(TAG, "onAddStream: $p0")
                        if (p0 != null) {
                            if (p0.audioTracks.size > 0) {
                                rtcClient?.localAudioTrack = p0.audioTracks[0]
                            }
                        }
                        //TODO rewrite this code
                        lifecycleScope.launch {
                            delay(15000)
                            //TODO fullscreen impl deprecated
                            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
                            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                            binding.blackContainer.visibility = View.VISIBLE
                        }
                        //p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
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
                        Log.e(TAG, "onTrack: $transceiver")
                    }
                }
            )



            rtcClient?.initSurfaceView(binding.remoteView)
            rtcClient?.startLocalVideoCapture(binding.remoteView)

            //TODO streams stops but camera not disabled
            viewModel.state.observe(viewLifecycleOwner) {
                when (it) {
                    "watching" -> {
                        Log.d("asdasd", "watching")
                        rtcClient?.localVideoTrack?.setEnabled(true)
                    }
                    "idle" -> {
                        Log.d("asdasd", "idle")
                        rtcClient?.localVideoTrack?.setEnabled(false)
                    }
                }
            }

            signallingClient = SignalingClient(meetId, object : SignalingClientListener {
                override fun onConnectionEstablished() {
                    //end_call_button.isClickable = true
                }

                override fun onOfferReceived(description: SessionDescription) {
                    rtcClient?.onRemoteSessionReceived(description)
                    Constants.isIntiatedNow = false
                    //rtcClient?.answer(sdpObserver,meetId)
                    //remote_view_loading.isGone = true
                }

                override fun onAnswerReceived(description: SessionDescription) {
                    rtcClient?.onRemoteSessionReceived(description)
                    Constants.isIntiatedNow = false
                    //remote_view_loading.isGone = true
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
            rtcClient?.call(sdpObserver, meetId)
            rtcClient?.enableAudio(true)
            //TODO rename Timer class
            val micTimer = Timer(lifecycleScope, requireContext())
            micTimer.startTimer()
            lifecycleScope.launch {
                micTimer.flow.debounce(1000).collectLatest {
                    Firebase.auth.currentUser?.uid?.let {
                        Firebase.firestore.collection("notify").document(it)
                            .set(hashMapOf("timestamp" to System.currentTimeMillis()))
                    }
                }
            }
        }
        return binding.root
    }

    private val sdpObserver = object : AppSdpObserver() {
        override fun onCreateSuccess(p0: SessionDescription?) {
            super.onCreateSuccess(p0)
//            signallingClient.send(p0)
        }
    }
}