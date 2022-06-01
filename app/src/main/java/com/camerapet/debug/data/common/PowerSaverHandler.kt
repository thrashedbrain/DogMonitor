package com.camerapet.debug.data.common

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.camerapet.debug.R

class PowerSaverHandler constructor(private val context: Context) {

    enum class DozeState{
        CHECKED, IGNORED, UNSUPPORTED
    }

    fun check(): DozeState {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            DozeState.UNSUPPORTED
        } else {
            if (isIgnoringBatteryOptimizations(context)) {
                DozeState.CHECKED
            } else {
                DozeState.IGNORED
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun startBatteryOptimizationIntent() {
        val name = context.resources.getString(R.string.app_name)
        Toast.makeText(context, "Battery optimization -> All apps -> $name -> Don't optimize", Toast.LENGTH_LONG).show()

        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        //intent.data = "package:${context.packageName}".toUri()
        context.startActivity(intent)
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pwrm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = context.applicationContext.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pwrm.isIgnoringBatteryOptimizations(name)
        }
        return true
    }
}