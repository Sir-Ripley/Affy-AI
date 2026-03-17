# AGENTS-IMPROVEMENT-SPEC.md

## Purpose

This document records what is good, what is missing, and what is broken in the current Affy-AI codebase, followed by a concrete improvement plan.

---

## What Is Good

- **Architecture is coherent.** The Freudian triad (Id / Superego / Ego) maps cleanly to distinct Kotlin classes. The separation of concerns is sound.
- **Event-driven service.** `QuantumEyesService` is purely callback-based — no polling, no background threads. Correct for battery efficiency on the Pixel 9A.
- **WorkManager constraints.** `DreamCycleWorker` correctly requires `setRequiresDeviceIdle(true)` and `setRequiresCharging(true)`. The nightly schedule is properly unique-named with `KEEP` policy.
- **Memory decay math.** `QuantumEgoArbiter.encodeQagMemory()` and `DreamCycleWorker.processDailyDreams()` both implement the Temporal Echo formula with indexed decay — the math is correct.
- **TTS lifecycle.** `AffinionHandler` initializes TTS in `initializeBrain()` and shuts it down in `closeBrain()`, which is called from `onDestroy()`. No leak.
- **Gradle dependencies.** `build.gradle.kts` declares LiteRT, WorkManager, AppCompat, and Material — all required libraries are present.
- **Dark UI theme.** `activity_main.xml` uses `#121212` background with correct Material color tokens.

---

## What Is Missing

1. **LiteRT inference is not wired.** `AffinionHandler.processIncomingVibration()` contains a hardcoded string instead of running either TFLite model. The `Interpreter` instances are initialized but never called.
2. **Chi-squared blend is not applied.** `QuantumEgoArbiter.findMiddleGround()` exists but is never called from `AffinionHandler`. The Ego arbiter is disconnected from the main processing pipeline.
3. **Dream Cycle trigger button is absent.** The README specifies a Button in `activity_main.xml` to manually trigger `DreamCycleWorker` for testing. The layout does not include it.
4. **`AGENTS.md` did not exist.** No agent context file was present to guide AI-assisted development. (Now created.)
5. **No `.gitignore`.** The repo has no `.gitignore`. Android build artifacts (`build/`, `*.apk`, `local.properties`, `.gradle/`) and IDE files (`.idea/`) are unprotected from accidental commits.
6. **No `local.properties` guard.** `local.properties` (which contains the local SDK path) is not excluded.
7. **`AndroidManifest.xml` is missing `RECEIVE_BOOT_COMPLETED`.** WorkManager requires this permission on some Android versions to reschedule periodic work after reboot.
8. **No token/prompt pipeline.** There is no tokenizer or prompt-formatting layer between raw notification text and the `Interpreter`. LiteRT Gemma models require tokenized input tensors, not raw strings.
9. **No error UI feedback.** Model load failures and TTS init failures are logged but never surfaced to the user.
10. **`devcontainer.json` has no Android toolchain.** The dev container uses a universal image with no Android SDK, `adb`, or Gradle wrapper. Building the project requires manual setup.

---

## What Is Broken

| File | Issue | Severity |
|---|---|---|
| `MainActivity.kt` | `scheduleREMSleep()` is defined twice — the outer function body contains a second `private fun scheduleREMSleep()` declaration. This is a compile error. | ❌ Blocks build |
| `QuantumEgoArbiter.kt` | Missing `package com.yourdomain.affy` declaration at the top. The class will not resolve correctly in the package. | ❌ Blocks build |
| `MainActivity.kt` | `Settings.ACTION_ACTION_LISTENER_SETTINGS` is not a valid constant. The correct constant is `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`. | ❌ Runtime crash |
| `AddAndroidManifest.xml` | Contains raw Kotlin code (not XML). The file extension is wrong and the content is a duplicate of `QuantumEyesService.kt`. | ⚠️ Confusing |
| `AdroidServicetag.xml` | Duplicate service tag fragment — already present in `AndroidManifest.xml`. Serves no purpose. | ⚠️ Confusing |
| `.json` | Unnamed file at repo root. Not referenced by any build file. Likely a test-case scratch file. | ⚠️ Noise |

---

## Improvement Plan

### Priority 1 — Fix compile and runtime blockers

**1.1 Fix `MainActivity.kt` nested function**

Remove the outer `scheduleREMSleep()` shell. The inner definition is the correct one and should be the only one.

```kotlin
// REMOVE the outer wrapper:
private fun scheduleREMSleep() {
    private fun scheduleREMSleep() { // <-- this nested declaration is the bug
        ...
    }
// KEEP only the inner body, promoted to the class level.
```

**1.2 Add package declaration to `QuantumEgoArbiter.kt`**

```kotlin
package com.yourdomain.affy
```

**1.3 Fix the Settings intent constant in `MainActivity.kt`**

```kotlin
// Wrong:
val intent = Intent(Settings.ACTION_ACTION_LISTENER_SETTINGS)
// Correct:
val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
```

---

### Priority 2 — Wire the Ego pipeline

**2.1 Connect `QuantumEgoArbiter` to `AffinionHandler`**

`AffinionHandler` should instantiate `QuantumEgoArbiter` and call `findMiddleGround()` after receiving both model outputs. Until real inference is available, the simulated float values from the test `.json` file can serve as stand-ins.

**2.2 Implement LiteRT inference stub**

Replace the hardcoded `simulatedResponse` string in `processIncomingVibration()` with a proper inference call structure:

```kotlin
// Allocate input/output buffers per the model's tensor spec
// Run: tfliteInterpreterId?.run(inputBuffer, outputBuffer)
// Decode output tokens back to a string
```

The exact tensor shapes depend on the `.tflite` model files (not yet in the repo). Document the expected input shape in `AffinionHandler.kt` as a comment once models are added.

**2.3 Add tokenizer layer**

Gemma TFLite models require SentencePiece tokenization. Add a `Tokenizer` helper class or use the LiteRT `BertTokenizer` / `SentencePieceTokenizer` from `com.google.ai.edge.litert:litert-support` as a dependency.

---

### Priority 3 — Complete the UI

**3.1 Add Dream Cycle trigger button to `activity_main.xml`**

```xml
<Button
    android:id="@+id/btnTriggerDream"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Trigger Dream Cycle"
    android:backgroundTint="#BB86FC"
    android:textColor="#000000" />
```

Wire it in `MainActivity.kt` to enqueue a one-time `DreamCycleWorker` request.

**3.2 Add error feedback for model load failure**

Show a `Snackbar` or `Toast` when `AffinionHandler.initializeBrain()` catches an exception, so the user knows the models are missing rather than seeing a silent failure.

---

### Priority 4 — Manifest and permissions

**4.1 Add `RECEIVE_BOOT_COMPLETED` permission**

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

**4.2 Add `POST_NOTIFICATIONS` permission (Android 13+)**

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

### Priority 5 — Repository hygiene

**5.1 Create `.gitignore`**

```
# Android
*.apk
*.aab
*.ap_
*.dex
local.properties
.gradle/
build/
captures/
.externalNativeBuild/
.cxx/

# IDE
.idea/
*.iml
*.iws
.DS_Store

# Keys
*.jks
*.keystore
```

**5.2 Remove or relocate scratch files**

- Delete `AddAndroidManifest.xml` — it is not valid XML and duplicates `QuantumEyesService.kt`.
- Delete `AdroidServicetag.xml` — the service tag is already in `AndroidManifest.xml`.
- Move `.json` to a `test-data/` directory or delete it.

---

### Priority 6 — Dev container

**6.1 Add Android SDK to `devcontainer.json`**

Switch from the universal image to a Java-based image and add the Android SDK feature, or add a `postCreateCommand` that installs the Android command-line tools:

```json
{
  "image": "mcr.microsoft.com/devcontainers/java:21",
  "features": {
    "ghcr.io/devcontainers/features/android-sdk:1": {}
  },
  "postCreateCommand": "echo 'sdk.dir=/usr/local/android-sdk' > local.properties"
}
```

This allows `./gradlew assembleDebug` to run inside the environment without manual SDK setup.

---

## Summary of Changes by File

| File | Action |
|---|---|
| `MainActivity.kt` | Fix nested `scheduleREMSleep()`, fix Settings constant, add Dream button handler |
| `AffinionHandler.kt` | Wire `QuantumEgoArbiter`, replace hardcoded response with inference stub |
| `QuantumEgoArbiter.kt` | Add package declaration |
| `activity_main.xml` | Add Dream Cycle trigger button |
| `AndroidManifest.xml` | Add `RECEIVE_BOOT_COMPLETED` and `POST_NOTIFICATIONS` permissions |
| `build.gradle.kts` | Add `litert-support` for tokenizer |
| `.gitignore` | Create |
| `.devcontainer/devcontainer.json` | Add Android SDK |
| `AddAndroidManifest.xml` | Delete |
| `AdroidServicetag.xml` | Delete |
| `.json` | Move to `test-data/` or delete |
