package com.boat.hearables.pro.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class OutputChannel(val label: String) {
    LEFT_ONLY("Left Bud Only"),
    RIGHT_ONLY("Right Bud Only"),
    BOTH_STEREO("Both Earbuds"),
    OUT_OF_PHASE("Polarity Phase Check")
}

class AcousticTestGenerator {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var activeJob: Job? = null
    private var activeTrack: AudioTrack? = null

    fun stop() {
        activeJob?.cancel()
        activeJob = null
        runCatching {
            activeTrack?.pause()
            activeTrack?.flush()
            activeTrack?.release()
        }
        activeTrack = null
    }

    /**
     * Plays a Continuous Sine Tone at [frequencyHz] routed to [channel] for driver/sweep/stereo testing.
     */
    fun playContinuousTone(frequencyHz: Float, channel: OutputChannel, amplitude: Float = 0.65f) {
        stop()
        activeJob = scope.launch {
            val sampleRate = 48000
            val frameCount = sampleRate / 10 // 100ms chunks
            val buffer = ShortArray(frameCount * 2) // Stereo interleaved L, R
            val track = createStereoTrack(sampleRate, buffer.size * 2)
            activeTrack = track
            track.play()

            var phase = 0.0
            val phaseStep = 2.0 * PI * frequencyHz / sampleRate

            while (isActive) {
                for (i in 0 until frameCount) {
                    val sample = (sin(phase) * Short.MAX_VALUE * amplitude).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    val inverted = (-sample).toShort()
                    when (channel) {
                        OutputChannel.LEFT_ONLY -> {
                            buffer[i * 2] = sample
                            buffer[i * 2 + 1] = 0
                        }
                        OutputChannel.RIGHT_ONLY -> {
                            buffer[i * 2] = 0
                            buffer[i * 2 + 1] = sample
                        }
                        OutputChannel.BOTH_STEREO -> {
                            buffer[i * 2] = sample
                            buffer[i * 2 + 1] = sample
                        }
                        OutputChannel.OUT_OF_PHASE -> {
                            buffer[i * 2] = sample
                            buffer[i * 2 + 1] = inverted
                        }
                    }
                    phase += phaseStep
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                track.write(buffer, 0, buffer.size)
            }
        }
    }

    /**
     * Plays a high-visibility 2.8 kHz - 4.2 kHz modulated acoustic beacon on Left, Right, or Both earbuds
     * to help locate a misplaced earbud in a room.
     */
    fun playFindMyBudsChirp(channel: OutputChannel) {
        stop()
        activeJob = scope.launch {
            val sampleRate = 48000
            val chunkSamples = sampleRate / 10
            val buffer = ShortArray(chunkSamples * 2)
            val track = createStereoTrack(sampleRate, buffer.size * 2)
            activeTrack = track
            track.play()

            var tick = 0
            var phase = 0.0
            while (isActive) {
                val freq = if (tick % 4 < 2) 3100.0 else 4100.0
                val isSilentGap = (tick % 6 == 5)
                val step = 2.0 * PI * freq / sampleRate

                for (i in 0 until chunkSamples) {
                    val sample = if (isSilentGap) 0 else (sin(phase) * Short.MAX_VALUE * 0.90f).toInt().toShort()
                    buffer[i * 2] = if (channel == OutputChannel.RIGHT_ONLY) 0 else sample
                    buffer[i * 2 + 1] = if (channel == OutputChannel.LEFT_ONLY) 0 else sample
                    phase += step
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                track.write(buffer, 0, buffer.size)
                tick++
            }
        }
    }

    /**
     * Plays an alternating 55 Hz Sub-Bass and 500 Hz Mid-Range reference tone so the user can
     * compare sub-bass pressure and verify airtight ear-tip seal.
     */
    fun playEarTipSealTest(onComplete: () -> Unit) {
        stop()
        activeJob = scope.launch {
            val sampleRate = 48000
            val chunkSamples = sampleRate / 10
            val buffer = ShortArray(chunkSamples * 2)
            val track = createStereoTrack(sampleRate, buffer.size * 2)
            activeTrack = track
            track.play()

            val sequenceFreqs = listOf(55.0, 55.0, 55.0, 500.0, 500.0, 55.0, 55.0, 500.0)
            var phase = 0.0
            for (freq in sequenceFreqs) {
                if (!isActive) break
                val step = 2.0 * PI * freq / sampleRate
                repeat(5) { // 500ms per step
                    for (i in 0 until chunkSamples) {
                        val s = (sin(phase) * Short.MAX_VALUE * 0.75f).toInt().toShort()
                        buffer[i * 2] = s
                        buffer[i * 2 + 1] = s
                        phase += step
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }
                    track.write(buffer, 0, buffer.size)
                }
            }
            delay(100)
            stop()
            onComplete()
        }
    }

    private fun createStereoTrack(sampleRate: Int, minBytes: Int): AudioTrack {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(minBytes)

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
    }
}
