package com.example.pulse

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool

class AudioManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var ambientId: Int = 0
    private var chimeId: Int = 0
    private var whooshId: Int = 0
    private var burstId: Int = 0
    private var muted = false

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        // 60 BPM heartbeat: 72 bpm => 0.833s per beat, but rhythm is 0.8s
        ambientId = soundPool!!.load(generateTone(60f, 4f, 0.06f), 0)
        chimeId = soundPool!!.load(generateTone(520f, 0.35f, 0.18f), 0)
        whooshId = soundPool!!.load(generateTone(220f, 1.2f, 0.2f), 0)
        burstId = soundPool!!.load(generateTone(523f, 0.5f, 0.12f), 0)
    }

    private fun generateTone(freq: Float, durationSec: Float, volume: Float): ByteArray {
        val sr = 44100
        val n = (sr * durationSec).toInt()
        val buf = ByteArray(n * 2)
        for (i in 0 until n) {
            val t = i.toFloat() / sr
            val sine = (Math.sin((2 * Math.PI * freq * t).toDouble()) * 0.25f * volume).toFloat()
            val s = (sine * 32767).toShort()
            buf[i * 2] = s.toByte()
            buf[i * 2 + 1] = (s shr 8).toByte()
        }
        return buf
    }

    fun playChime() { if (!muted) soundPool?.play(chimeId, 0.5f, 0.5f, 1, 0, 1f) }
    fun playWhoosh() { if (!muted) soundPool?.play(whooshId, 0.4f, 0.4f, 1, 0, 1f) }
    fun playBurst() {
        if (!muted) {
            soundPool?.play(burstId, 0.6f, 0.6f, 1, 0, 1f)
            soundPool?.play(whooshId, 0.3f, 0.3f, 1, 0, 1.1f)
        }
    }
    fun playAmbient() { if (!muted) soundPool?.play(ambientId, 0.12f, 0.12f, 0, -1, 1f) }
    fun stopAmbient() { soundPool?.stop(ambientId) }

    fun toggleMute() {
        muted = !muted
        if (muted) soundPool?.autoPause() else soundPool?.autoResume()
    }
    fun isMuted() = muted

    fun release() { soundPool?.release(); soundPool = null }

    fun vibrate(ms: Long = 50) {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        v?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(android.os.VibrationEffect.createOneShot(ms, 50))
            } else {
                @Suppress("DEPRECATION") it.vibrate(ms)
            }
        }
    }
}