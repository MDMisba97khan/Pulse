package com.example.pulse

import android.content.Context
import com.example.pulse.model.Orb
import com.example.pulse.model.Shape

class GameEngine(
    private val audio: AudioManager,
    private val context: Context,
) {
    var orb = Orb()
        private set
    private val shapes = mutableListOf<Shape>()

    private var screenW = 1080f
    private var screenH = 1920f
    private var running = false
    private var muted = false

    var score = 0
        private set
    var timeRemaining = 45f
        private set
    var lost = false
        private set
    var won = false
        private set
    var darkenAlpha = 0f
        private set

    var onScore: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null
    var onWin: (() -> Unit)? = null
    var onTick: ((Float) -> Unit)? = null

    private var lastDriftLeft = true
    private var pulsePhase = 0f
    private var nextBeatTime = 0L

    fun init(w: Float, h: Float) {
        screenW = w; screenH = h
        orb.reset(screenW, screenH)
        shapes.clear()
        score = 0; timeRemaining = 45f
        lost = false; won = false
        darkenAlpha = 0f; lastDriftLeft = true
        pulsePhase = 0f
    }

    fun start() { running = true; audio.playAmbient() }
    fun stop() { running = false; audio.stopAmbient() }
    fun toggleMute() { muted = !muted; audio.toggleMute() }

    /** Call on tap. Returns true if a heartbeat tone should be scheduled. */
    fun onTap(tapX: Float): Boolean {
        if (lost || won) return false
        val now = System.nanoTime()
        val driftLeft = tapX < screenW / 2f
        lastDriftLeft = driftLeft

        orb.vx = if (driftLeft) -80f else 80f
        orb.vy = 0f
        orb.hue = if (driftLeft) 220f else 160f

        // Sync to next beat (0.8s interval)
        nextBeatTime = now + ((800_000_000 - (System.nanoTime() % 800_000_000L)).coerceAtLeast(0L))

        pulsePhase = 0f
        shapes.add(Shape.spawnOpposite(screenW, screenH, driftLeft, 0.8f))
        return true
    }

    fun update(dt: Float): Boolean {
        if (!running || lost || won) return false

        timeRemaining -= dt
        onTick?.invoke(timeRemaining.coerceAtLeast(0f))

        if (timeRemaining <= 0f) { checkWin(); return true }

        // Orb physics
        orb.x += orb.vx * dt
        orb.y += orb.vy * dt
        orb.x = orb.x.coerceIn(orb.radius, screenW - orb.radius)
        orb.y = orb.y.coerceIn(orb.radius, screenH - orb.radius)
        orb.vx *= 0.98f; orb.vy *= 0.98f
        orb.radius = 28f + (Math.sin(System.currentTimeMillis() * 0.003).toFloat() * 4f)
        orb.glowRadius = orb.radius * 2f

        // Shapes drift
        val iter = shapes.iterator()
        while (iter.hasNext()) {
            val s = iter.next()
            s.grow(dt)
            s.drift(dt)
            if (s.alive && s.collidesWith(orb.x, orb.y, orb.radius)) {
                onHit(s); return true
            }
            if (s.offScreen(screenW, screenH)) { s.alive = false; iter.remove() }
        }

        // Pulse beat every 0.8s (60bpm)
        pulsePhase += dt * 1.25f
        if (pulsePhase > 1f) pulsePhase %= 1f

        if (won) return true
        if (lost) { darkenAlpha = (darkenAlpha + dt * 2f).coerceAtMost(0.7f) }
        return false
    }

    fun onSurvivedTap() {
        if (lost || won) return
        score++
        onScore?.invoke(score)
        if (score >= 10) checkWin()
    }

    private fun onHit(s: Shape) {
        lost = true
        audio.stopAmbient()
        audio.playChime()
        audio.vibrate(100)
        darkenAlpha = 0.3f
        onLose?.invoke()
    }

    private fun checkWin() {
        if (score >= 10 && !won) {
            won = true
            audio.playBurst()
            audio.vibrate(200)
            onWin?.invoke()
        }
    }

    fun getShapes() = shapes.toList()
}