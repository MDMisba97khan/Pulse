package com.example.pulse.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Shape(
    var centerX: Float = 0f,
    var centerY: Float = 0f,
    var baseRadius: Float = 40f,
    var currentRadius: Float = 10f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var hue: Float = 180f,
    var controlOffset: Float = 0f,
    var phase: Float = 0f,
    var alive: Boolean = true,
    var opacity: Float = 0.5f,
    var spawnTime: Float = 0f,
) {
    fun grow(dt: Float, rate: Float = 25f) {
        currentRadius = (currentRadius + rate * dt).coerceAtMost(baseRadius)
    }

    fun drift(dt: Float) {
        centerX += vx * dt
        centerY += vy * dt
        phase += dt * 1.2f
    }

    fun collidesWith(orbX: Float, orbY: Float, orbRadius: Float): Boolean {
        val dx = orbX - centerX
        val dy = orbY - centerY
        val r = currentRadius + orbRadius * 0.6f
        return dx * dx + dy * dy < r * r
    }

    fun offScreen(screenW: Float, screenH: Float): Boolean {
        return centerX < -150f || centerX > screenW + 150f ||
               centerY < -150f || centerY > screenH + 150f
    }

    companion object {
        fun spawnOpposite(
            screenW: Float, screenH: Float, driftLeft: Boolean, rhythm: Float = 0.8f
        ): Shape {
            val fromRight = driftLeft
            val cx = if (fromRight) screenW + 80f else -80f
            val cy = (Math.random() * screenH * 0.7 + screenH * 0.15).toFloat()
            val speed = 35f + (Math.random() * 35f).toFloat()
            val vx = if (fromRight) -speed else speed
            val vy = ((screenW / 2f - cx) / 4f).coerceIn(-25f, 25f)
            return Shape(
                centerX = cx,
                centerY = cy,
                baseRadius = 30f + (Math.random() * 25f).toFloat(),
                currentRadius = 4f,
                vx = vx,
                vy = vy,
                hue = (Math.random() * 50f + 170f).toFloat(),
                controlOffset = 15f + (Math.random() * 25f).toFloat(),
                phase = (Math.random() * 6.28f).toFloat(),
                alive = true,
                opacity = 0.45f + (Math.random() * 0.25f).toFloat(),
                spawnTime = rhythm,
            )
        }
    }
}