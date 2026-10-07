package com.example.pulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulse.GameEngine
import com.example.pulse.Shape
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(engine: GameEngine) {

    var secondsLeft by remember { mutableFloatStateOf(45f) }
    var currentScore by remember { mutableIntStateOf(0) }
    var lost by remember { mutableStateOf(false) }
    var won by remember { mutableStateOf(false) }
    var showRestart by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        engine.init(1080f, 1920f)
        engine.onScore = { currentScore = it }
        engine.onLose = { lost = true; showRestart = true }
        engine.onWin = { won = true; showRestart = true }
        engine.onTick = { secondsLeft = it }
        engine.start()
    }

    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (!lost && !won) {
            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.1f)
            lastTime = now
            engine.update(dt)
            kotlinx.coroutines.delay(16)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (!lost && !won) {
                        engine.onTap(offset.x)
                        engine.onSurvivedTap()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGame(engine, secondsLeft, currentScore, lost, won)
        }
    }

    if (showRestart) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = if (won) "✨ Perfect Run ✨" else "Lost Your Rhythm", fontSize = 32.sp, color = Color.White)
        }
        LaunchedEffect(showRestart) {
            kotlinx.coroutines.delay(2000)
            engine.init(1080f, 1920f)
            currentScore = 0; secondsLeft = 45f; lost = false; won = false; showRestart = false
            engine.start()
        }
    }
}

private fun DrawScope.drawGame(engine: GameEngine, secondsLeft: Float, score: Int, lost: Boolean, won: Boolean) {
    val w = size.width; val h = size.height
    drawRect(brush = Brush.linearGradient(listOf(Color(0xFF0a0a1a), Color(0xFF12122a), Color(0xFF0d0d20)), Offset.Zero, Offset(0f, h)))

    for (s in engine.getShapes()) drawOrganicShape(s)

    drawOrb(engine)

    // Pulse overlay: screen pulses with heartbeat
    val beat = Math.sin(engine.pulsePhase * 2 * PI).toFloat()
    val pulseScale = 1f + beat * 0.03f
    drawRect(Color.Black.copy(alpha = engine.darkenAlpha * (1f - beat * 0.3f)))

    if (lost) {
        drawRect(Color.Black.copy(alpha = engine.darkenAlpha))
    }
}

private fun DrawScope.drawOrganicShape(shape: Shape) {
    val cx = shape.centerX; val cy = shape.centerY; val r = shape.currentRadius; val hue = shape.hue; val a = shape.opacity
    val c = Color.HSVToColor(floatArrayOf(hue, 0.5f, 0.6f).also { it[2] = a })
    drawCircle(color = c.copy(alpha = a * 0.3f), radius = r * 1.8f, center = Offset(cx, cy))

    val path = Path()
    val pts = 12; val ph = shape.phase
    for (i in 0..pts) {
        val ang = (i.toFloat() / pts) * 2f * PI.toFloat()
        val v = 0.7f + 0.3f * sin(ph + i * 0.9f).toFloat()
        val px = cx + r * v * cos(ang); val py = cy + r * v * sin(ang)
        if (i == 0) path.moveTo(px, py)
        else {
            val pa = ((i - 1).toFloat() / pts) * 2f * PI.toFloat()
            val pv = 0.7f + 0.3f * sin(ph + (i - 1) * 0.9f).toFloat()
            val pr = r * pv
            val ppx = cx + pr * cos(pa); val ppy = cy + pr * sin(pa)
            val mx = (ppx + px) / 2f; val my = (ppy + py) / 2f
            path.quadraticBezierTo(ppx + shape.controlOffset * cos(ang - 1.2f).toFloat(), ppy + shape.controlOffset * sin(ang - 1.2f).toFloat(), mx, my)
        }
    }
    path.close()
    drawPath(path = path, color = c, style = Fill)
    drawCircle(color = Color.White.copy(alpha = a * 0.15f), radius = r * 0.4f, center = Offset(cx, cy))
}

private fun DrawScope.drawOrb(engine: GameEngine) {
    val o = engine.orb; val hue = o.hue
    drawCircle(color = Color.HSVToColor(floatArrayOf(hue, 0.8f, 1f).also { it[2] = 0.4f }), radius = o.glowRadius, center = Offset(o.x, o.y))
    drawCircle(color = Color.HSVToColor(floatArrayOf(hue, 0.9f, 1f)), radius = o.radius, center = Offset(o.x, o.y))
    drawCircle(color = Color.White.copy(alpha = 0.5f), radius = o.radius * 0.35f, center = Offset(o.x - o.radius * 0.2f, o.y - o.radius * 0.2f))
}