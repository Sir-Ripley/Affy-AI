# AGENTS-IMPROVEMENT-SPEC.md

Concrete improvement plan for the Affy-AI codebase, based on a full audit of all source files.

---

## What's Good

- **Architecture is coherent.** The Freudian triad (Id / Superego / Ego) maps cleanly to `AffinionHandler` (Ego singleton), two TFLite models, and `QuantumEgoArbiter` (math layer). The separation of concerns is sound.
- **Event-driven design is correct.** `QuantumEyesService` extends `NotificationListenerService` with no polling loop — battery-safe as intended.
- **WorkManager constraints are correct.** `DreamCycleWorker` enforces `setRequiresDeviceIdle(true)` + `setRequiresCharging(true)` — the nightly REM constraint is properly implemented.
- **TTS lifecycle is handled.** `AffinionHandler` implements `OnInitListener` and `closeBrain()` is called in `onDestroy()`.
- **`AndroidManifest.xml` is structurally correct.** Service declaration, permission binding, and launcher intent-filter are all present.
- **`QuantumEgoArbiter.kt` math is implemented.** Both the Chi-Squared conflict resolution and the temporal echo memory decay are present and algorithmically correct.
- **`DreamCycleWorker.processDailyDreams()` applies the decay formula correctly** using `echoDecayRate.pow(index)` over a reversed list.

---

## What's Missing

### 1. No `AGENTS.md`
**Status:** Fixed in this session — `AGENTS.md` created.

### 2. No "Trigger Dream Cycle" button in `activity_main.xml`
The README spec explicitly requires a `Button` to manually trigger `DreamCycleWorker` for testing. The layout file has no such button, and `MainActivity.kt` has no corresponding click handler.

**Fix:** Add a `Button` with `id="@+id/btnTriggerDream"` to `activity_main.xml` and wire it in `MainActivity.onCreate()` to enqueue a one-time `WorkRequest` for `DreamCycleWorker`.

### 3. Actual LiteRT inference not wired in `AffinionHandler`
`processIncomingVibration()` logs the prompt contexts but returns a hardcoded string. The `tfliteInterpreterId` and `tfliteInterpreterSuperego` interpreters are initialized but never called.

**Fix:** Implement `runInference(interpreter: Interpreter, prompt: String): String` that tokenizes the prompt, runs `interpreter.run()`, and decodes the output buffer. Call it for both hemispheres, then pass results to `QuantumEgoArbiter.findMiddleGround()` to produce the blended response.

### 4. `QuantumEgoArbiter` not integrated into `AffinionHandler`
`QuantumEgoArbiter` exists as a standalone class but is never instantiated or called from `AffinionHandler`. The Chi-Squared blending and memory encoding are dead code.

**Fix:** Instantiate `QuantumEgoArbiter` inside `AffinionHandler` (or pass it in). After both model inferences return numeric confidence scores, call `arbiter.findMiddleGround()` to resolve the final response weight, then call `arbiter.encodeQagMemory()` to persist the state.

### 5. No `checkNotificationPermission()` helper
The README spec requires a `checkNotificationPermission(context)` helper in `QuantumEyesService` that gracefully redirects to Android Settings on `SecurityException`. This function is absent.

**Fix:** Add the helper to `QuantumEyesService`:
```kotlin
fun checkNotificationPermission(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    )
    return enabledListeners?.contains(context.packageName) == true
}
```
Call it from `MainActivity` before toggling the Switch on.

### 6. No complete `build.gradle.kts`
The file is a bare `dependencies {}` block with no `plugins`, `android {}`, or `defaultConfig` sections. It cannot be used as-is.

**Fix:** Expand to a full module-level `build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.yourdomain.affy"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.yourdomain.affy"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("com.google.ai.edge.litert:litert:1.0.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}
```

### 7. No `settings.gradle.kts` or root `build.gradle.kts`
A buildable Android project requires a `settings.gradle.kts` (declaring the app module) and a root-level `build.gradle.kts` (declaring plugin versions). Neither exists.

**Fix:** Create both files. Minimum `settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories { google(); mavenCentral() }
}
rootProject.name = "Affy"
include(":app")
```

### 8. No proper Android project directory structure
All `.kt` and `.xml` files are at the repository root. Android Studio and Gradle expect:
```
app/
  src/main/
    java/com/yourdomain/affy/   ← .kt files
    res/layout/                 ← .xml layouts
    assets/                     ← .tflite models
    AndroidManifest.xml
  build.gradle.kts
settings.gradle.kts
build.gradle.kts
```
**Fix:** Migrate all source files into the standard Gradle project layout. This is the single highest-priority structural fix — without it the project cannot be built by Android Studio or CI.

---

## What's Wrong (Bugs)

### BUG-1: `MainActivity.kt` — nested duplicate function (compile error)
`scheduleREMSleep()` is declared twice: the outer declaration has an empty body, and the inner declaration is nested inside it. This is a Kotlin syntax error that prevents compilation.

```kotlin
// BROKEN — as found in the file:
private fun scheduleREMSleep() {
        private fun scheduleREMSleep() {   // ← nested, illegal
    val dreamConstraints = ...
    ...
    }
// Missing closing brace for outer function
```

**Fix:** Remove the outer empty declaration and keep only the inner implementation, properly closed:
```kotlin
private fun scheduleREMSleep() {
    val dreamConstraints = Constraints.Builder()
        .setRequiresDeviceIdle(true)
        .setRequiresCharging(true)
        .build()
    val nightlyREMRequest = PeriodicWorkRequestBuilder<DreamCycleWorker>(24, TimeUnit.HOURS)
        .setConstraints(dreamConstraints)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "AffyDailyDream",
        ExistingPeriodicWorkPolicy.KEEP,
        nightlyREMRequest
    )
}
```

### BUG-2: `QuantumEgoArbiter.kt` — missing `package` declaration
The file has no `package com.yourdomain.affy` header. The Kotlin compiler will place it in the default package, causing `AffinionHandler` (which is in `com.yourdomain.affy`) to be unable to reference it without a fully-qualified import.

**Fix:** Add `package com.yourdomain.affy` as the first line.

### BUG-3: `MainActivity.kt` — wrong Settings action constant
```kotlin
val intent = Intent(Settings.ACTION_ACTION_LISTENER_SETTINGS)
```
`Settings.ACTION_ACTION_LISTENER_SETTINGS` does not exist. The correct constant is `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.

**Fix:**
```kotlin
val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
```

### BUG-4: `AndroidManifest.xml` — missing `<uses-permission>` for TTS
`AffinionHandler` uses `TextToSpeech`. While TTS itself doesn't require a manifest permission, the app reads notification content which may include sensitive data. More critically, `NotificationListenerService` requires the manifest to declare it correctly — the current manifest is missing the `android:exported="false"` guard for internal components and has no `<queries>` block for Android 11+ package visibility if needed.

**Fix (immediate):** Verify the service binding permission is correct. The current declaration is valid for `NotificationListenerService`. No change needed here beyond confirming it.

### BUG-5: `AffinionHandler.kt` — `initializeBrain()` called on main thread
`Interpreter(loadModelFile(...))` performs file I/O and model loading synchronously. Called from `MainActivity.onCreate()`, this blocks the main thread and will trigger an `ANR` on large models (the 2B parameter Id model will be especially slow).

**Fix:** Wrap `initializeBrain()` in a coroutine or `AsyncTask`-equivalent:
```kotlin
// In MainActivity.onCreate():
lifecycleScope.launch(Dispatchers.IO) {
    AffinionHandler.initializeBrain(this@MainActivity)
}
```
Add `androidx.lifecycle:lifecycle-runtime-ktx` to `build.gradle.kts`.

### BUG-6: `DreamCycleWorker.kt` — hardcoded daily vibration list
`processDailyDreams()` operates on `listOf(0.8, 0.92, 0.5, 0.97)` — a static placeholder. There is no mechanism to accumulate actual `psiQagT` values from `QuantumEgoArbiter` during the day and pass them to the worker.

**Fix:** Persist daily states to `SharedPreferences` or a Room database in `QuantumEgoArbiter.encodeQagMemory()`, and read them back in `DreamCycleWorker.processDailyDreams()`. Clear the store after consolidation.

---

## Stale / Redundant Files to Delete

| File | Reason |
|---|---|
| `AddAndroidManifest.xml` | Duplicate of the `QuantumEyesService` service tag already in `AndroidManifest.xml`. Scratch file. |
| `AdroidServicetag.xml` | Same — a fragment of the service XML tag, superseded by `AndroidManifest.xml`. |

These files will confuse agents and developers. Delete them.

---

## Priority Order

| Priority | Item |
|---|---|
| P0 | Fix BUG-1 (`MainActivity.kt` compile error — nested function) |
| P0 | Fix BUG-2 (`QuantumEgoArbiter.kt` missing package declaration) |
| P0 | Fix BUG-3 (wrong Settings action constant) |
| P0 | Migrate to standard Android project directory structure |
| P0 | Complete `build.gradle.kts` + add `settings.gradle.kts` |
| P1 | Add "Trigger Dream Cycle" button to `activity_main.xml` + `MainActivity` |
| P1 | Add `checkNotificationPermission()` to `QuantumEyesService` |
| P1 | Fix BUG-5 (move `initializeBrain()` off main thread) |
| P2 | Wire `QuantumEgoArbiter` into `AffinionHandler` |
| P2 | Implement actual LiteRT inference in `processIncomingVibration()` |
| P2 | Fix BUG-6 (persist daily states for `DreamCycleWorker`) |
| P3 | Delete `AddAndroidManifest.xml` and `AdroidServicetag.xml` |
| P3 | Move `.json` test stimuli to `test/` or `assets/test/` |
