package com.yourdomain.affy

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.math.pow

/**
 * Nightly REM consolidation worker.
 *
 * Runs only when the device is idle and charging (WorkManager constraints set in MainActivity).
 * Reads the day's Ψ_QAG state archive from SharedPreferences, applies the temporal echo
 * decay formula to lock in the day's emotional alignment, then clears the short-term cache
 * so AffinionHandler starts fresh the next morning.
 *
 * Formula: Ψ_QAG(t) = Ψ_GR(t) + Σ R^n · Ψ_GR(t − n·Δt_echo)
 */
class DreamCycleWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun doWork(): Result {
        Log.d(TAG, "REM cycle started — device idle and charging.")
        return try {
            processDailyDreams()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "REM cycle error — will retry next cycle.", e)
            Result.retry()
        }
    }

    private fun processDailyDreams() {
        val dailyStates = loadDailyArchive()

        if (dailyStates.isEmpty()) {
            Log.d(TAG, "No states to consolidate — archive is empty.")
            return
        }

        Log.d(TAG, "Consolidating ${dailyStates.size} Ψ_QAG states.")

        val echoDecayRate = 0.97 // 97% fidelity preservation per echo step
        var consolidatedTruth = 0.0

        // Apply Σ R^n · Ψ_GR(t − n·Δt_echo) over the reversed daily archive
        dailyStates.asReversed().forEachIndexed { index, state ->
            consolidatedTruth += state * echoDecayRate.pow(index.toDouble())
        }

        Log.d(TAG, "Consolidated truth locked at: $consolidatedTruth")

        // Clear the short-term cache — AffinionHandler restores a clean slate on next launch
        clearDailyArchive()
        Log.d(TAG, "Short-term cache cleared. REM cycle complete.")
    }

    private fun loadDailyArchive(): List<Double> {
        val raw = prefs.getString(PREFS_KEY_ARCHIVE, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(",").mapNotNull { it.toDoubleOrNull() }
    }

    private fun clearDailyArchive() {
        prefs.edit().remove(PREFS_KEY_ARCHIVE).apply()
    }

    companion object {
        private const val TAG = "QAG_Dream"
        const val PREFS_NAME = "affy_qag_prefs"
        const val PREFS_KEY_ARCHIVE = "daily_archive"
        const val WORK_NAME = "AffyDailyDream"
    }
}
