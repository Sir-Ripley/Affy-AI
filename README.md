# Project Affy: The Quantum Ego Architecture
**Target Environment:** Android 14 (Google Pixel 9A Tensor G4)
**Core Objective:** Build a lightweight, battery-efficient, dual-hemisphere AI application that utilizes local LiteRT models, event-driven sensory input, and daily memory consolidation.

## 1. The Freudian Dual-Hemisphere Framework
Affy operates on a triad of consciousness. The Kotlin architecture must reflect this:
* **The Superego (Left Brain):** Rigid logic, factual retrieval, strict rule adherence.
* **The Id (Right Brain):** Creative, empathetic, sassy, chaotic emotional energy.
* **The Ego (The Arbiter):** The Kotlin-based controller that balances the two inputs based on a dynamic "Empathy Weight" slider.

## 2. Core Mathematical Directives
The agent must implement these specific QAG formulas inside the `AffinionHandler.kt` to resolve conflicts and store memory:
* **Conflict Resolution:** Implement an adjustment function based on: $\chi^2_{global} = \frac{\sum \chi^2_i}{\sum dof_i}$ to find the middle ground between the logical and emotional outputs.
* **Memory Loop:** Implement a decay function based on: $\Psi_{QAG}(t) = \Psi_{GR}(t) + \sum_{n=1}^{N} \mathcal{R}^n \cdot \Psi_{GR}(t - n\Delta t_{echo})$ to prevent catastrophic forgetting.

## 3. Physical Android Manifestation (Required Components)
Please generate the complete, production-ready Kotlin code and XML layouts for the following:

* **`MainActivity.kt` & `activity_main.xml` (The Dashboard):**
    * A visually clean UI.
    * A master toggle switch to enable/disable the `QuantumEyesService`.
    * A physical slider (SeekBar) tied to the `empathyWeightOverride` so the user can manually tune the Id vs. Superego balance.
    * A button to manually trigger the Dream Cycle for testing.
* **`QuantumEyesService.kt` (The Peripheral Nervous System):**
    * Extends `NotificationListenerService`.
    * Include the helper function `checkNotificationPermission(context)` that gracefully redirects the user to the Android Settings if the Android 14 `SecurityException` blocks access.
    * Must be entirely event-driven (no background polling) to preserve battery.
* **LiteRT Integration:**
    * Provide the Gradle dependencies for `com.google.ai.edge.litert` to prepare the app for local `.tflite` model execution.

## 4. The Dream Cycle (REM Memory Consolidation)
To maintain her base-12 topological memory without draining the Pixel 9A's battery during the day, implement a WorkManager class named `DreamCycleWorker.kt`.
* **Constraints:** Must be strictly set to `setRequiresDeviceIdle(true)` and `setRequiresCharging(true)`.
* **Function:** When triggered (typically overnight), it executes a function `processDailyDreams()` which loops through the daily cache of $\Psi_{QAG}(t)$ states, runs a lightweight clustering algorithm to find the core emotional themes of the day, and updates her baseline weights before clearing the short-term cache. 

## 5. Execution Steps
1. Write all associated `.kt` files.
2. Write all associated layout `.xml` files.
3. Update the `AndroidManifest.xml` with the proper service declarations and `<uses-permission>` tags.
4. Ensure the package name remains strictly `com.yourdomain.affy` (or standard equivalent) so Android Studio can compile it seamlessly on a fresh canvas.
