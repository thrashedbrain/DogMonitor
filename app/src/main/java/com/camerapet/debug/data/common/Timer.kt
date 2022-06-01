package com.camerapet.debug.data.common

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

class Timer constructor(private val scope: CoroutineScope, private val context: Context) {

    private var job: Job? = null
    var recorder: MediaRecorder? = null

    private val _flow = MutableSharedFlow<Int>(1, 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val flow = _flow.asSharedFlow()

    fun startTimer() {
        stopTimer()
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder?.setOutputFile(context)
        recorder?.prepare()
        recorder?.start()
        job = scope.launch {
            while (isActive) {
                if (recorder != null) {
                    if (recorder!!.maxAmplitude > 7000) {
                        Log.d("asdasdasd", "asdasdasdasdasdasdasdasd")
                        _flow.emit(recorder!!.maxAmplitude)
                    }
                }
                delay(2500)
            }
        }
    }

    fun stopTimer() {
        recorder?.stop()
        recorder?.reset()
        recorder?.release()
        job?.cancel()
        job = null
    }

    fun MediaRecorder.setOutputFile(context: Context) {
        val tmpRecordingFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setOutputFile(File(tmpRecordingFolder, "recording.mp3"))
        } else {
            setOutputFile("/dev/null")
        }
    }
}