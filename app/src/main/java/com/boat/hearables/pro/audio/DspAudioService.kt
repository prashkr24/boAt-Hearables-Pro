package com.boat.hearables.pro.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.os.IBinder
import com.boat.hearables.pro.MainActivity
import com.boat.hearables.pro.R
import com.boat.hearables.pro.data.AppPreferences
import com.boat.hearables.pro.data.BoatDeviceCatalog
import com.boat.hearables.pro.data.RoomPlacement
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * Dedicated DSP audio engine for the boAt Nirvana Luxe speaker.
 * Synchronizes the 7 native BES hardware equalizer bands
 * (`60Hz, 120Hz, 250Hz, 750Hz, 2000Hz, 5000Hz, 15000Hz`) and Room Placement compensation
 * with Android's system-wide `DynamicsProcessing`, `BassBoost`, `Virtualizer`, and `LoudnessEnhancer`.
 */
object DspAudioController {

    private val dynamicsMap = ConcurrentHashMap<Int, DynamicsProcessing>()
    private val legacyEqMap = ConcurrentHashMap<Int, Equalizer>()
    private val bassBoostMap = ConcurrentHashMap<Int, BassBoost>()
    private val virtualizerMap = ConcurrentHashMap<Int, Virtualizer>()
    private val loudnessMap = ConcurrentHashMap<Int, LoudnessEnhancer>()

    private val activeSessions = ConcurrentHashMap.newKeySet<Int>().apply { add(0) }

    fun registerAudioSession(sessionId: Int, context: Context) {
        if (sessionId < 0) return
        activeSessions.add(sessionId)
        applyFromPreferences(context)
    }

    fun unregisterAudioSession(sessionId: Int) {
        if (sessionId <= 0) return
        activeSessions.remove(sessionId)
        runCatching { dynamicsMap.remove(sessionId)?.release() }
        runCatching { legacyEqMap.remove(sessionId)?.release() }
        runCatching { bassBoostMap.remove(sessionId)?.release() }
        runCatching { virtualizerMap.remove(sessionId)?.release() }
        runCatching { loudnessMap.remove(sessionId)?.release() }
    }

    fun applyFromPreferences(context: Context) {
        val prefs = AppPreferences(context)
        applyDspState(
            enabled = prefs.hardwareEqEnabled,
            gainsDb = prefs.currentBandGains,
            roomPlacement = prefs.roomPlacement,
            preAmpDb = prefs.preAmpDb,
            limiterEnabled = prefs.limiterEnabled,
            bassBoostPercent = prefs.bassBoostPercent,
            subBassCutoffHz = prefs.subBassCutoffHz,
            virtualizerPercent = prefs.virtualizerPercent,
            loudnessPercent = prefs.loudnessPercent
        )
    }

    fun applyDspState(
        enabled: Boolean,
        gainsDb: List<Float>,
        roomPlacement: RoomPlacement,
        preAmpDb: Float,
        limiterEnabled: Boolean,
        bassBoostPercent: Int,
        subBassCutoffHz: Int,
        virtualizerPercent: Int,
        loudnessPercent: Int
    ) {
        // 7 BES Band upper cutoff boundaries matching 60, 120, 250, 750, 2000, 5000, 15000 Hz
        val cutoffFreqs = floatArrayOf(85f, 175f, 450f, 1250f, 3200f, 8500f, 20000f)
        val bassExtraDb = (bassBoostPercent / 100f) * 5.0f

        for (sessionId in activeSessions) {
            // 1. Android 7-Band DynamicsProcessing matched to Nirvana Luxe BES DSP
            runCatching {
                val dp = dynamicsMap.getOrPut(sessionId) {
                    val cfg = DynamicsProcessing.Config.Builder(
                        DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                        2,
                        true,
                        7, // 7 native Nirvana Luxe bands
                        false,
                        0,
                        true,
                        7,
                        true
                    ).build()
                    DynamicsProcessing(0, sessionId, cfg).apply { this.enabled = true }
                }

                dp.setInputGainAllChannelsTo(if (enabled) preAmpDb else 0f)

                for (b in 0 until 7) {
                    val baseGain = if (enabled) gainsDb.getOrElse(b) { 0f } else 0f
                    val centerHz = BoatDeviceCatalog.LUXE_EQ_FREQUENCIES_HZ[b]
                    val roomOffset = when (b) {
                        0, 1 -> roomPlacement.bassTiltDb
                        2 -> roomPlacement.bassTiltDb * 0.4f
                        5, 6 -> roomPlacement.trebleTiltDb
                        else -> 0f
                    }
                    val subBoost = if (enabled && centerHz <= subBassCutoffHz) bassExtraDb
                    else if (enabled && centerHz <= 120) bassExtraDb * 0.5f else 0f

                    val totalGain = (baseGain + roomOffset + subBoost).coerceIn(-15f, 15f)
                    val band = DynamicsProcessing.EqBand(enabled, cutoffFreqs[b], totalGain)
                    dp.setPreEqBandAllChannelsTo(b, band)
                    dp.setPostEqBandAllChannelsTo(b, DynamicsProcessing.EqBand(true, cutoffFreqs[b], 0f))
                }

                val limiter = DynamicsProcessing.Limiter(
                    true,
                    limiterEnabled,
                    0,
                    1.5f,
                    70.0f,
                    10.0f,
                    -0.5f,
                    0.0f
                )
                dp.setLimiterAllChannelsTo(limiter)
            }

            // 2. Hardware/System Equalizer companion
            runCatching {
                val eq = legacyEqMap.getOrPut(sessionId) {
                    Equalizer(0, sessionId).apply { this.enabled = true }
                }
                val numBands = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange
                val minLevel = range[0].toInt()
                val maxLevel = range[1].toInt()
                for (b in 0 until numBands) {
                    val centerFreqHz = (eq.getCenterFreq(b.toShort()) / 1000).coerceAtLeast(30)
                    val nearestIdx = BoatDeviceCatalog.LUXE_EQ_FREQUENCIES_HZ.indices.minByOrNull { idx ->
                        kotlin.math.abs(BoatDeviceCatalog.LUXE_EQ_FREQUENCIES_HZ[idx] - centerFreqHz)
                    } ?: 0
                    val db = if (enabled) gainsDb.getOrElse(nearestIdx) { 0f } else 0f
                    val millibels = (db * 100f).roundToInt().coerceIn(minLevel, maxLevel).toShort()
                    eq.setBandLevel(b.toShort(), millibels)
                }
            }

            // 3. Passive Radiator BassBoost
            runCatching {
                val bb = bassBoostMap.getOrPut(sessionId) {
                    BassBoost(0, sessionId).apply { this.enabled = true }
                }
                if (bb.strengthSupported) {
                    bb.setStrength((if (enabled) bassBoostPercent * 10 else 0).coerceIn(0, 1000).toShort())
                }
            }

            // 4. 360° Omnidirectional Virtualizer
            runCatching {
                val virt = virtualizerMap.getOrPut(sessionId) {
                    Virtualizer(0, sessionId).apply { this.enabled = true }
                }
                if (virt.strengthSupported) {
                    virt.setStrength((if (enabled) virtualizerPercent * 10 else 0).coerceIn(0, 1000).toShort())
                }
            }

            // 5. Party LoudnessEnhancer
            runCatching {
                val le = loudnessMap.getOrPut(sessionId) {
                    LoudnessEnhancer(sessionId).apply { this.enabled = true }
                }
                le.setTargetGain((if (enabled) loudnessPercent * 12 else 0).coerceIn(0, 1200))
            }
        }
    }
}

class DspAudioService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        DspAudioController.applyFromPreferences(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = AppPreferences(this)
        val notification = buildNotification(prefs.activePresetId, prefs.soundMode.label)
        startForeground(NOTIFICATION_ID, notification)
        DspAudioController.applyFromPreferences(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "boAt Nirvana Luxe DSP Studio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active 7-Band BES Hardware EQ & Speaker Mastering Engine"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(presetId: String, soundModeLabel: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("boAt Nirvana Luxe • Speaker Studio Active")
            .setContentText("$soundModeLabel • Profile: ${presetId.uppercase()}")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "boat_nirvana_luxe_dsp"
        private const val NOTIFICATION_ID = 2402
    }
}
