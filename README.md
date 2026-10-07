# Pulse

A 45-second minimalist micro-game: tap the glowing orb, hear your own heartbeat echoed back, and let the screen pulse in rhythm with it.

## Build

```bash
./gradlew :app:assembleDebug
```

## Run

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Structure

```
Pulse/
├── app/build.gradle.kts
├── AndroidManifest.xml
└── java/com/example/pulse/
    ├── MainActivity.kt
    ├── AudioManager.kt
    ├── engine/GameEngine.kt
    └── ui/GameScreen.kt
```

## Rules

- Tap to pulse the orb + heartbeat tone at 60 BPM (0.8s rhythm)
- Organic shapes spawn opposite to drift and sync to the beat
- 10 perfect taps → radiant celebration
- 45s auto-restart

## Tech

Kotlin, Jetpack Compose, Canvas, SoundPool sine-wave audio. Zero external deps.