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
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * Singleton DSP engine that attaches Android's modern 10-Band `DynamicsProcessing`
 * (plus `Equalizer`, `BassBoost`, `Virtualizer`, and `LoudnessEnhancer`) to global output (session 0)
 * and all active media player audio sessions.
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
            gainsDb = prefs.currentBandGains,
            preAmpDb = prefs.preAmpDb,
            limiterEnabled = prefs.limiterEnabled,
            bassBoostPercent = prefs.bassBoostPercent,
            subBassCutoffHz = prefs.subBassCutoffHz,
            virtualizerPercent = prefs.virtualizerPercent,
            loudnessPercent = prefs.loudnessPercent
        )
    }

    fun applyDspState(
        gainsDb: List<Float>,
        preAmpDb: Float,
        limiterEnabled: Boolean,
        bassBoostPercent: Int,
        subBassCutoffHz: Int,
        virtualizerPercent: Int,
        loudnessPercent: Int
    ) {
        val cutoffFreqs = floatArrayOf(
            45f, 90f, 180f, 355f, 710f, 1400f, 2800f, 5600f, 11200f, 20000f
        )
        val bassExtraDb = (bassBoostPercent / 100f) * 6.0f

        for (sessionId in activeSessions) {
            // 1. Modern Android 10-Band DynamicsProcessing (API 28+)
            runCatching {
                val dp = dynamicsMap.getOrPut(sessionId) {
                    val cfg = DynamicsProcessing.Config.Builder(
                        DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                        2, // stereo channels
                        true, // pre-EQ
                        10,   // 10 pre-EQ bands
                        false, // multi-band compressor
                        0,
                        true, // post-EQ
                        10,   // 10 post-EQ bands
                        true  // anti-clipping limiter
                    ).build()
                    DynamicsProcessing(0, sessionId, cfg).apply { enabled = true }
                }

                dp.setInputGainAllChannelsTo(preAmpDb)

                for (b in 0 until 10) {
                    val baseGain = gainsDb.getOrElse(b) { 0f }
                    val centerHz = BoatDeviceCatalog.EQ_FREQUENCIES_HZ[b]
                    val subBoost = if (centerHz <= subBassCutoffHz) bassExtraDb else if (centerHz <= 125) bassExtraDb * 0.5f else 0f
                    val totalBandGain = (baseGain + subBoost).coerceIn(-15f, 15f)

                    val band = DynamicsProcessing.EqBand(true, cutoffFreqs[b], totalBandGain)
                    dp.setPreEqBandAllChannelsTo(b, band)
                    dp.setPostEqBandAllChannelsTo(b, DynamicsProcessing.EqBand(true, cutoffFreqs[b], 0f))
                }

                val limiter = DynamicsProcessing.Limiter(
                    true,
                    limiterEnabled,
                    0,
                    1.0f,   // attack 1ms
                    60.0f,  // release 60ms
                    10.0f,  // 10:1 ratio
                    -0.5f,  // threshold -0.5 dBFS
                    0.0f    // post-gain 0 dB
                )
                dp.setLimiterAllChannelsTo(limiter)
            }

            // 2. Hardware/System Equalizer fallback & companion
            runCatching {
                val eq = legacyEqMap.getOrPut(sessionId) {
                    Equalizer(0, sessionId).apply { enabled = true }
                }
                val numBands = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange
                val minLevel = range[0].toInt()
                val maxLevel = range[1].toInt()
                for (b in 0 until numBands) {
                    val centerFreqHz = (eq.getCenterFreq(b.toShort()) / 1000).coerceAtLeast(20)
                    val nearestIdx = BoatDeviceCatalog.EQ_FREQUENCIES_HZ.indices.minByOrNull { idx ->
                        kotlin.math.abs(BoatDeviceCatalog.EQ_FREQUENCIES_HZ[idx] - centerFreqHz)
                    } ?: 0
                    val db = gainsDb.getOrElse(nearestIdx) { 0f }
                    val millibels = (db * 100f).roundToInt().coerceIn(minLevel, maxLevel).toShort()
                    eq.setBandLevel(b.toShort(), millibels)
                }
            }

            // 3. BassBoost (0..1000)
            runCatching {
                val bb = bassBoostMap.getOrPut(sessionId) {
                    BassBoost(0, sessionId).apply { enabled = true }
                }
                if (bb.strengthSupported) {
                    bb.setStrength((bassBoostPercent * 10).coerceIn(0, 1000).toShort())
                }
            }

            // 4. 360° Spatial Virtualizer (0..1000)
            runCatching {
                val virt = virtualizerMap.getOrPut(sessionId) {
                    Virtualizer(0, sessionId).apply { enabled = true }
                }
                if (virt.strengthSupported) {
                    virt.setStrength((virtualizerPercent * 10).coerceIn(0, 1000).toShort())
                }
            }

            // 5. LoudnessEnhancer (0..1200 mB)
            runCatching {
                val le = loudnessMap.getOrPut(sessionId) {
                    LoudnessEnhancer(sessionId).apply { enabled = true }
                }
                le.setTargetGain((loudnessPercent * 12).coerceIn(0, 1200))
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
        val notification = buildNotification(prefs.selectedDeviceName, prefs.activePresetId)
        startForeground(NOTIFICATION_ID, notification)
        DspAudioController.applyFromPreferences(this)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "boAt Hearables Pro DSP Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active 10-band Custom EQ & Hearable Protocol Service"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(deviceName: String, presetId: String): Notification {
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
            .setContentTitle("$deviceName • 10-Band DSP Active")
            .setContentText("EQ Profile: ${presetId.uppercase()} • Zero-Ad Pro Engine")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "boat_hearables_pro_dsp"
        private const val NOTIFICATION_ID = 2401
    }
}
