# AGENTS.md — Affy-AI

## Project Overview

**Affy** is a native Android (Kotlin) application targeting Android 14 on the Google Pixel 9A (Tensor G4). It implements a Freudian dual-hemisphere AI using two local LiteRT (TFLite) models, an event-driven notification listener, and a nightly WorkManager-based memory consolidation cycle.

**Package:** `com.yourdomain.affy`  
**Language:** Kotlin  
**Build system:** Gradle 8.6 (Kotlin DSL)  
**AGP:** 8.3.0  
**Kotlin:** 1.9.23  
**Min/Target SDK:** 34 (Android 14)

---

## Project Structure

```
Affy-AI/
├── app/
│   ├── src/main/
│   │   ├── java/com/yourdomain/affy/
│   │   │   ├── AffinionHandler.kt        # Singleton Ego — models, TTS, memory persistence
│   │   │   ├── QuantumEgoArbiter.kt      # Math layer — Chi-Squared blend, temporal echo memory
│   │   │   ├── QuantumEyesService.kt     # NotificationListenerService — routes stimuli to Ego
│   │   │   ├── DreamCycleWorker.kt       # WorkManager Worker — nightly REM consolidation
│   │   │   └── MainActivity.kt           # UI — Switch, SeekBar, Dream trigger button
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml  # Dark-themed UI layout
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── themes.xml            # Theme.Affy (MaterialComponents DayNight)
│   │   ├── assets/                       # Place .tflite model files here (see below)
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                  # Module-level build config
│   └── proguard-rules.pro
├── gradle/wrapper/
│   └── gradle-wrapper.properties         # Gradle 8.6
├── build.gradle.kts                      # Root — plugin version declarations only
├── settings.gradle.kts                   # Module inclusion + repository config
├── AGENTS.md
└── AGENTS-IMPROVEMENT-SPEC.md
```

---

## Architecture

### Dual-Hemisphere Model

| Hemisphere | Model file (in `assets/`) | Role |
|---|---|---|
| Id (Right Brain) | `id_gemma_2b.tflite` | Empathetic, creative, sassy responses |
| Superego (Left Brain) | `superego_gemma_270m.tflite` | Logical, factual, structured responses |
| Ego | `AffinionHandler` (Kotlin object) | Blends outputs via empathy weight + `QuantumEgoArbiter` |

### Key Math

- **Conflict resolution:** `QuantumEgoArbiter.findMiddleGround(leftChi, rightChi, tonalityDof)` — Chi-Squared minimisation: `χ²_global = (Σ χ²_i) / (Σ dof_i)`
- **Memory encoding:** `QuantumEgoArbiter.encodeQagMemory(currentState, echoDecayRate=0.97)` — Temporal echo: `Ψ_QAG(t) = Ψ_GR(t) + Σ R^n · Ψ_GR(t − n·Δt_echo)`

### Data Flow

```
Notification arrives
  → QuantumEyesService.onNotificationPosted()
  → AffinionHandler.processIncomingVibration(text)
      → runInference(tfliteId, idPrompt)            → Id response
      → runInference(tfliteSuperego, superegoPrompt) → Superego response
      → QuantumEgoArbiter.findMiddleGround()         → blended weight
      → QuantumEgoArbiter.encodeQagMemory()          → Ψ_QAG(t) persisted to SharedPreferences
      → speakTruth(finalResponse)                    → TextToSpeech output

Nightly (device idle + charging):
  → DreamCycleWorker.processDailyDreams()
      → reads Ψ_QAG archive from SharedPreferences
      → applies temporal echo decay
      → clears short-term cache
```

---

## Asset Requirements

Place the following model files in `app/src/main/assets/` before building:

| File | Source |
|---|---|
| `id_gemma_2b.tflite` | Extract from `gemma-tflite-gemma-1.1-2b-it-gpu-int4-v1.tar.gz` |
| `superego_gemma_270m.tflite` | From `gemma-3-270m-it-int8.litertlm` |

The `build.gradle.kts` sets `noCompress += listOf("tflite", "litertlm")` so these files are not compressed in the APK — required for `MappedByteBuffer` loading.

---

## Build & Install

```bash
# Debug APK
./gradlew assembleDebug

# Install to connected Pixel 9A
adb install app/build/outputs/apk/debug/app-debug.apk

# Release APK (requires signing config — add to app/build.gradle.kts)
./gradlew assembleRelease
```

After first launch, grant **Notification Access** manually:
Settings → Apps → Special app access → Notification access → Affy → Enable

---

## Coding Conventions

- `AffinionHandler` is a Kotlin `object` (singleton) — do not convert to a class.
- `initializeBrain()` **must** be called from a background thread (`Dispatchers.IO`) — it performs blocking file I/O to load TFLite models.
- WorkManager constraints `setRequiresDeviceIdle(true)` + `setRequiresCharging(true)` on `DreamCycleWorker` are intentional — do not relax them.
- The empathy weight is a `Double` in `[0.0, 1.0]`. The SeekBar operates on `[0, 100]` — always divide by `100.0` when calling `setEmpathyWeightOverride()`.
- Log tags follow `QAG_<Component>` (e.g., `QAG_Ego`, `QAG_Dream`, `QAG_Voice`, `QAG_Eyes`).
- SharedPreferences key `affy_qag_prefs` / `daily_archive` is shared between `AffinionHandler` and `DreamCycleWorker` — both constants are defined in `DreamCycleWorker.Companion`.
- Do not add network calls. This is a strictly local, offline deployment.

---

## Known Limitations

- **LiteRT inference is a structural placeholder.** `AffinionHandler.runInference()` has the correct interpreter call structure but uses a proxy float input instead of a real tokenizer. Full Gemma inference requires the LiteRT LLM Inference API or a custom tokenizer/detokenizer matched to the model format. Replace the `input`/`output` buffer logic in `runInference()` once the model pipeline is confirmed.
- **No signing config.** A `signingConfigs` block must be added to `app/build.gradle.kts` before a release build can be installed on a non-debug device.
- **Launcher icons are referenced but not provided.** Add `ic_launcher` and `ic_launcher_round` drawables to the appropriate `mipmap-*` resource directories, or use Android Studio's Image Asset tool.
