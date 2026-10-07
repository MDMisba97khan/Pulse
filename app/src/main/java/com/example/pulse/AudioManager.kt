package com.example.pulse

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import java.io.File

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

        val cacheDir = context.cacheDir
        val ambientFile = File(cacheDir, "ambient.wav")
        writeTone(ambientFile, 60f, 4f, 0.06f)
        val f1 = File(cacheDir, "chime.wav")
        writeTone(f1, 520f, 0.35f, 0.18f)
        val f2 = File(cacheDir, "whoosh.wav")
        writeTone(f2, 220f, 1.2f, 0.2f)
        val f3 = File(cacheDir, "burst.wav")
        writeTone(f3, 523f, 0.5f, 0.12f)

        ambientId = soundPool!!.load(ambientFile.absolutePath, 0)
        chimeId = soundPool!!.load(f1.absolutePath, 0)
        whooshId = soundPool!!.load(f2.absolutePath, 0)
        burstId = soundPool!!.load(f3.absolutePath, 0)
    }

    private fun writeTone(file: File, freq: Float, durationSec: Float, volume: Float) {
        val sr = 44100
        val n = (sr * durationSec).toInt()
        val buf = ByteArray(n * 2)
        for (i in 0 until n) {
            val t = i.toFloat() / sr
            val sine = (Math.sin((2 * Math.PI * freq * t).toDouble()) * 0.25f * volume).toFloat()
            val s = (sine * 32767).toShort()
            buf[i * 2] = s.toByte()
            buf[i * 2 + 1] = (s ushr 8).toByte()
        }
        file.writeBytes(String(buf).decodeToString())
        file.setWritable(true)
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