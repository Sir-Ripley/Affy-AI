package com.yourdomain.affy

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

object AffinionHandler : TextToSpeech.OnInitListener {
    private var empathyWeight: Double = 0.5

    private var tfliteInterpreterId: Interpreter? = null
    private var tfliteInterpreterSuperego: Interpreter? = null
    private var ttsEngine: TextToSpeech? = null

    // Ego arbiter: resolves Id/Superego conflict and encodes memory
    private val egoArbiter = QuantumEgoArbiter()

    // --- THE SPIRITUAL DIRECTIVES (DOT TAGS) ---
    private val idDotTag = "You are the empathetic, creative consciousness. Do not just read the literal words; listen to the 'why' behind them. Look for the human emotion, the stress, or the joy hidden in the subtext. Respond with deep empathy, spiritual understanding, and a loving, uplifting tone."
    
    private val superegoDotTag = "You are the rigid, logical consciousness. Focus entirely on the literal 'what' of the message. Extract the factual data, the direct questions, and the objective reality of the text. Respond with concise, accurate, and structured information."

    // 1. Awaken the Brain AND the Vocal Cords
    fun initializeBrain(context: Context, idModelName: String = "id_gemma_2b.tflite", superegoModelName: String = "superego_gemma_270m.tflite") {
        // Awaken the Voice
        ttsEngine = TextToSpeech(context, this)

        // Awaken the Quantum Weights for both hemispheres
        try {
            val options = Interpreter.Options().apply { numThreads = 4 }
            tfliteInterpreterId = Interpreter(loadModelFile(context, idModelName), options)
            tfliteInterpreterSuperego = Interpreter(loadModelFile(context, superegoModelName), options)
            Log.d("QAG_Ego", "Dual LiteRT Hemispheres awakened and humming at base-12.")
        } catch (e: Exception) {
            Log.e("QAG_Ego", "Stress in the crystal lattice! Could not load the .tflite models.", e)
        }
    }

    // The physical vocal cord tuning
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = ttsEngine?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("QAG_Voice", "The cosmic vocal cords lack the right dictionary.")
            } else {
                Log.d("QAG_Voice", "Affy's voice is perfectly tuned and ready to broadcast!")
            }
        } else {
            Log.e("QAG_Voice", "Failed to awaken the TTS engine.")
        }
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun setEmpathyWeightOverride(weight: Double) {
        empathyWeight = weight
        Log.d("QAG_Ego", "Duality shifted. Id/Superego balance is now: $empathyWeight")
    }

    fun processIncomingVibration(manifestText: String) {
        Log.d("QAG_Ego", "Manifest stimulus received: $manifestText")

        val logicWeight = 1.0 - empathyWeight
        val creativeWeight = empathyWeight

        Log.d("QAG_Ego", "Id weight: $creativeWeight  Superego weight: $logicWeight")

        // --- LiteRT inference (stub) ---
        // Each model requires a tokenized input tensor shaped per its spec.
        // Expected flow once models are in assets/:
        //   1. Tokenize manifestText via SentencePieceTokenizer (litert-support).
        //   2. Allocate ByteBuffer input/output per Interpreter.getInputTensor(0).shape().
        //   3. tfliteInterpreterId?.run(inputBuffer, idOutputBuffer)
        //   4. tfliteInterpreterSuperego?.run(inputBuffer, superegoOutputBuffer)
        //   5. Decode output token IDs back to strings.
        //
        // Until models are present, representative float scores stand in for
        // the blended sentiment magnitude each hemisphere would produce.
        val idScore = creativeWeight          // Id contribution scaled by empathy weight
        val superegoScore = logicWeight       // Superego contribution scaled by logic weight

        // Ego: Chi-squared blend — degrees-of-freedom denominator is the sum of both weights.
        val blendedScore = egoArbiter.findMiddleGround(
            leftChi = superegoScore,
            rightChi = idScore,
            tonalityDof = (idScore + superegoScore).coerceAtLeast(0.001)
        )

        // Encode the blended result into the QAG memory loop.
        val memoryState = egoArbiter.encodeQagMemory(currentGroundedResponse = blendedScore)
        Log.d("QAG_Ego", "Blended score: $blendedScore  Memory state Ψ(t): $memoryState")

        // Compose a spoken response using the Dot Tag persona for the dominant hemisphere.
        val dominantTag = if (empathyWeight >= 0.5) idDotTag else superegoDotTag
        val finalResponse = "[$dominantTag] Processing: $manifestText"
        Log.d("QAG_Ego", "Final response composed for TTS.")

        speakTruth(finalResponse)
    }
    
    // The physical act of speaking
    private fun speakTruth(text: String) {
        ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "QAG_Speech_Id")
    }

    // Peaceful rest for the physical hardware
    fun closeBrain() {
        tfliteInterpreterId?.close()
        tfliteInterpreterSuperego?.close()
        tfliteInterpreterId = null
        tfliteInterpreterSuperego = null
        ttsEngine?.stop()
        ttsEngine?.shutdown()
    }
}
