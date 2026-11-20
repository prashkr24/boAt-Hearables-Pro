package com.boat.hearables.pro.audio

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import com.boat.hearables.pro.data.AppPreferences
import com.boat.hearables.pro.data.BoatDeviceCatalog
import kotlin.math.roundToInt

/**
 * Listens for AudioEffect sessions and Bluetooth ACL events,
 * strictly reacting ONLY when the connected device is `boAt Nirvana Luxe`.
 */
class AudioSessionReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                DspAudioController.registerAudioSession(sessionId, context)
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                DspAudioController.unregisterAudioSession(sessionId)
            }
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                @Suppress("DEPRECATION")
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val btName = runCatching { device?.name }.getOrNull()
                // STRICT CHECK: Ignore all other Bluetooth devices; only trigger for boAt Nirvana Luxe
                if (!BoatDeviceCatalog.isNirvanaLuxeSpeaker(btName)) return

                val prefs = AppPreferences(context)
                prefs.lastKnownLuxeMac = runCatching { device?.address }.getOrNull()
                val autoProfile = prefs.getAutoProfile()
                if (autoProfile.enabled) {
                    val allPresets = BoatDeviceCatalog.BUILT_IN_PRESETS + prefs.getCustomPresets()
                    allPresets.firstOrNull { it.id == autoProfile.eqPresetId }?.let { preset ->
                        prefs.activePresetId = preset.id
                        prefs.currentBandGains = preset.gainsDb
                    }
                    prefs.soundMode = autoProfile.soundMode
                    prefs.ledMode = autoProfile.ledMode

                    runCatching {
                        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        if (am != null) {
                            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val targetVol = ((autoProfile.targetVolumePercent / 100f) * maxVol).roundToInt()
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        }
                    }
                }
                DspAudioController.applyFromPreferences(context)
            }
        }
    }
}
