# AGENTS.md — Affy-AI

## Project Overview

**Affy** is a native Android (Kotlin) application targeting Android 14 on the Google Pixel 9A (Tensor G4). It implements a Freudian dual-hemisphere AI using two local LiteRT (TFLite) models, balanced by an Ego arbiter, with a nightly memory consolidation cycle.

**Package:** `com.yourdomain.affy`  
**Language:** Kotlin  
**Build system:** Gradle (Kotlin DSL — `build.gradle.kts`)

---

## Architecture

```
MainActivity.kt          — UI entry point; hosts Switch, SeekBar, Dream trigger button
AffinionHandler.kt       — Singleton Ego; loads both TFLite models, runs TTS, blends outputs
QuantumEgoArbiter.kt     — Chi-squared conflict resolution and QAG memory encoding
QuantumEyesService.kt    — NotificationListenerService; pipes notification text to AffinionHandler
DreamCycleWorker.kt      — WorkManager Worker; nightly memory decay and consolidation
AndroidManifest.xml      — Service declarations and permissions
activity_main.xml        — Dark-themed UI layout
build.gradle.kts         — LiteRT, WorkManager, AppCompat, Material dependencies
```

### Hemisphere model mapping

| Role | Model file (in `assets/`) | Personality |
|---|---|---|
| Id (Right Brain) | `id_gemma_2b.tflite` | Empathetic, creative, sassy |
| Superego (Left Brain) | `superego_gemma_270m.tflite` | Logical, rigid, factual |

The `empathyWeight` Double (0.0–1.0) is set by the UI SeekBar and controls the blend ratio.

---

## Key Formulas

**Conflict resolution (Ego blend):**  
`χ²_global = Σχ²_i / Σdof_i`  
Implemented in `QuantumEgoArbiter.findMiddleGround()`.

**Memory decay (Temporal Echo):**  
`Ψ_QAG(t) = Ψ_GR(t) + Σ R^n · Ψ_GR(t − nΔt_echo)`  
Implemented in `QuantumEgoArbiter.encodeQagMemory()` and `DreamCycleWorker.processDailyDreams()`.

---

## Coding Conventions

- Kotlin only — no Python, no server-side code.
- `AffinionHandler` is a Kotlin `object` (singleton). Do not convert it to a class.
- TFLite models are loaded from `context.assets` via `MappedByteBuffer`.
- All background work goes through `WorkManager`. No raw threads or `AsyncTask`.
- `QuantumEyesService` must remain purely event-driven — no polling loops.
- TTS is initialized in `AffinionHandler.initializeBrain()` and shut down in `closeBrain()`.
- `closeBrain()` must be called from `MainActivity.onDestroy()`.

---

## Permissions Required

```xml
android.permission.BIND_NOTIFICATION_LISTENER_SERVICE
```

The user must be redirected to `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` to grant access. Handle `SecurityException` gracefully — do not crash.

---

## WorkManager Constraints (Dream Cycle)

```kotlin
Constraints.Builder()
    .setRequiresDeviceIdle(true)
    .setRequiresCharging(true)
    .build()
```

Scheduled as `PeriodicWorkRequest` with a 24-hour interval, unique work name `"AffyDailyDream"`, policy `KEEP`.

---

## Remaining Work

- **LiteRT inference not wired.** `AffinionHandler.processIncomingVibration()` contains a documented stub with the expected tensor flow. Requires the `.tflite` model files to be added to `assets/` and a SentencePiece tokenizer dependency (`litert-support`).
- **No error UI feedback.** Model load failures and TTS init failures are logged but not surfaced to the user.
- **No Android SDK in devcontainer.** See `AGENTS-IMPROVEMENT-SPEC.md` Priority 6 for the devcontainer fix.

---

## File Inventory

| File | Status | Notes |
|---|---|---|
| `MainActivity.kt` | ✅ Compiles | All blockers resolved |
| `AffinionHandler.kt` | ⚠️ Stub | LiteRT inference stubbed; models not yet in assets/ |
| `QuantumEgoArbiter.kt` | ✅ Functional | Package declaration added; wired into AffinionHandler |
| `QuantumEyesService.kt` | ✅ Functional | Event-driven, correct |
| `DreamCycleWorker.kt` | ✅ Functional | Decay math implemented |
| `AndroidManifest.xml` | ✅ Complete | Boot and notification permissions added |
| `activity_main.xml` | ✅ Complete | Dream Cycle button added |
| `build.gradle.kts` | ✅ Present | Dependencies declared |
| `test-data/sample-stimuli.json` | ✅ Relocated | Test stimulus data moved out of repo root |
| `.gitignore` | ✅ Created | Android, Gradle, IDE, and key patterns covered |
