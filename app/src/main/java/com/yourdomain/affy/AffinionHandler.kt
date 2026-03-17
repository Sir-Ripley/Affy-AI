package com.yourdomain.affy

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

/**
 * Singleton Ego — the Freudian arbiter between the Id and Superego hemispheres.
 *
 * Responsibilities:
 *  - Load and own both TFLite model interpreters.
 *  - Blend their outputs using a dynamic empathy weight via QuantumEgoArbiter.
 *  - Speak the final synthesized response via TextToSpeech.
 *  - Persist the QAG memory archive to SharedPreferences for cross-session continuity.
 *
 * Lifecycle: call initializeBrain() from a background thread (IO dispatcher).
 *            call closeBrain() in Activity.onDestroy().
 */
object AffinionHandler : TextToSpeech.OnInitListener {

    private const val TAG = "QAG_Ego"
    private const val PREFS_NAME = "affy_qag_prefs"
    private const val PREFS_KEY_ARCHIVE = "daily_archive"

    // Empathy weight: 0.0 = pure Superego (logic), 1.0 = pure Id (empathy)
    @Volatile private var empathyWeight: Double = 0.5

    private var tfliteId: Interpreter? = null
    private var tfliteSuperego: Interpreter? = null
    private var ttsEngine: TextToSpeech? = null
    private var prefs: SharedPreferences? = null

    val arbiter = QuantumEgoArbiter()

    // Dot-tag system prompts injected into each hemisphere's inference context
    private const val ID_DOT_TAG =
        "You are the empathetic, creative consciousness. Listen to the 'why' behind the words. " +
        "Find the human emotion, stress, or joy hidden in the subtext. " +
        "Respond with deep empathy, spiritual understanding, and a loving, uplifting tone."

    private const val SUPEREGO_DOT_TAG =
        "You are the rigid, logical consciousness. Focus entirely on the literal 'what' of the message. " +
        "Extract factual data, direct questions, and objective reality. " +
        "Respond with concise, accurate, and structured information."

    /**
     * Loads both TFLite models and initialises TTS.
     * Must be called from a background (IO) thread — model loading is blocking file I/O.
     */
    fun initializeBrain(
        context: Context,
        idModelName: String = "id_gemma_2b.tflite",
        superegoModelName: String = "superego_gemma_270m.tflite"
    ) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        restoreArchiveFromPrefs()

        // TTS init posts a callback to the main thread via OnInitListener — safe to call here
        ttsEngine = TextToSpeech(context.applicationContext, this)

        try {
            val options = Interpreter.Options().apply { numThreads = 4 }
            tfliteId = Interpreter(loadModelFile(context, idModelName), options)
            tfliteSuperego = Interpreter(loadModelFile(context, superegoModelName), options)
            Log.d(TAG, "Dual LiteRT hemispheres loaded.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite models — place .tflite files in app/src/main/assets/", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = ttsEngine?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("QAG_Voice", "TTS language data missing.")
            } else {
                Log.d("QAG_Voice", "TTS ready.")
            }
        } else {
            Log.e("QAG_Voice", "TTS initialisation failed.")
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        return FileInputStream(fd.fileDescriptor).channel
            .map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    fun setEmpathyWeightOverride(weight: Double) {
        empathyWeight = weight.coerceIn(0.0, 1.0)
        Log.d(TAG, "Empathy weight set to $empathyWeight")
    }

    /**
     * Main entry point for incoming stimuli (notifications, manual input).
     *
     * Flow:
     *  1. Build hemisphere-specific prompts using dot-tags.
     *  2. Run inference on both models (or fall back to simulated output if models not loaded).
     *  3. Derive Chi-Squared scores from the empathy weight.
     *  4. Blend via QuantumEgoArbiter and encode into QAG memory.
     *  5. Speak the result.
     */
    fun processIncomingVibration(manifestText: String) {
        Log.d(TAG, "Stimulus received: $manifestText")

        val idPrompt = "$ID_DOT_TAG\n\nUser Message: $manifestText"
        val superegoPrompt = "$SUPEREGO_DOT_TAG\n\nUser Message: $manifestText"

        val idResponse = runInference(tfliteId, idPrompt)
            ?: "[Id] I sense a lot of energy in that message. Let's approach it with care."
        val superegoResponse = runInference(tfliteSuperego, superegoPrompt)
            ?: "[Superego] Message received. Analysing content for actionable data."

        Log.d(TAG, "Id output: $idResponse")
        Log.d(TAG, "Superego output: $superegoResponse")

        // Derive Chi-Squared proxy scores from the empathy weight
        val idChi = empathyWeight
        val superegoChi = 1.0 - empathyWeight
        // tonalityDof = 1.0 normalises the result to [0, 1]
        val blendedWeight = arbiter.findMiddleGround(superegoChi, idChi, tonalityDof = 1.0)

        // Select the dominant response based on blended weight
        val finalResponse = if (blendedWeight >= 0.5) idResponse else superegoResponse

        // Encode this moment into the temporal echo memory and persist
        val psiQag = arbiter.encodeQagMemory(blendedWeight)
        persistArchiveToPrefs()
        Log.d(TAG, "Ψ_QAG(t) = $psiQag | Final response: $finalResponse")

        speakTruth(finalResponse)
    }

    /**
     * Runs a single inference pass through the given interpreter.
     * Returns null if the interpreter is not loaded (models not yet placed in assets).
     *
     * Note: This is a structural placeholder for the full tokenizer + decode pipeline.
     * LiteRT Gemma models require the LiteRT LLM Inference API or a custom tokenizer;
     * replace the input/output buffer logic here once the model format is confirmed.
     */
    private fun runInference(interpreter: Interpreter?, prompt: String): String? {
        interpreter ?: return null
        return try {
            // Input: single-element float array encoding prompt length as a proxy signal.
            // Replace with proper tokenization when integrating the full Gemma pipeline.
            val input = Array(1) { FloatArray(1) { prompt.length.toFloat() } }
            val output = Array(1) { FloatArray(1) }
            interpreter.run(input, output)
            // Placeholder decode: return the prompt summary until tokenizer is wired in.
            "Response to: ${prompt.take(60)}…"
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            null
        }
    }

    private fun speakTruth(text: String) {
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "QAG_Speech")
    }

    // --- SharedPreferences persistence for the QAG archive ---

    private fun persistArchiveToPrefs() {
        val snapshot = arbiter.getArchiveSnapshot()
        prefs?.edit()
            ?.putString(PREFS_KEY_ARCHIVE, snapshot.joinToString(","))
            ?.apply()
    }

    private fun restoreArchiveFromPrefs() {
        val raw = prefs?.getString(PREFS_KEY_ARCHIVE, "") ?: return
        if (raw.isBlank()) return
        val states = raw.split(",").mapNotNull { it.toDoubleOrNull() }
        arbiter.restoreArchive(states)
        Log.d(TAG, "Restored ${states.size} QAG states from prefs.")
    }

    /** Clears the daily archive after a REM consolidation cycle. */
    fun clearDailyArchive() {
        arbiter.clearArchive()
        prefs?.edit()?.remove(PREFS_KEY_ARCHIVE)?.apply()
    }

    /** Release all resources. Call from Activity.onDestroy(). */
    fun closeBrain() {
        tfliteId?.close()
        tfliteSuperego?.close()
        tfliteId = null
        tfliteSuperego = null
        ttsEngine?.stop()
        ttsEngine?.shutdown()
        ttsEngine = null
    }
}
