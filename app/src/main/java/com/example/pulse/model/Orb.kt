package com.example.pulse.model

data class Orb(
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var radius: Float = 28f,
    var glowRadius: Float = 56f,
    var hue: Float = 200f,
    var pulsePhase: Float = 0f,
) {
    fun reset(screenW: Float, screenH: Float) {
        x = screenW / 2f
        y = screenH / 2f
        vx = 0f
        vy = 0f
        radius = 28f
        glowRadius = radius * 2f
        hue = 200f
        pulsePhase = 0f
    }
}