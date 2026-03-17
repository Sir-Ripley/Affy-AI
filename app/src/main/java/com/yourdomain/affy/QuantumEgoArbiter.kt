package com.yourdomain.affy

import kotlin.math.pow

/**
 * Math layer for the Ego.
 *
 * Provides two operations:
 *  1. Chi-Squared conflict resolution — blends Id and Superego outputs into a single weight.
 *  2. Temporal echo memory encoding — applies decay over the session's state history to
 *     prevent catastrophic forgetting between REM cycles.
 *
 * Formula references:
 *   Conflict:  χ²_global = (Σ χ²_i) / (Σ dof_i)
 *   Memory:    Ψ_QAG(t) = Ψ_GR(t) + Σ R^n · Ψ_GR(t − n·Δt_echo)
 */
class QuantumEgoArbiter {

    // In-memory archive of Ψ_QAG states accumulated during the current session.
    // Persisted to SharedPreferences by AffinionHandler for cross-session use.
    private val memoryArchive = mutableListOf<Double>()

    /**
     * Resolves the conflict between the two hemispheres.
     *
     * @param leftChi   Chi-Squared score from the Superego (logic) hemisphere.
     * @param rightChi  Chi-Squared score from the Id (empathy) hemisphere.
     * @param tonalityDof  Degrees of freedom — typically the empathy weight denominator.
     * @return Unified frequency (blended weight).
     */
    fun findMiddleGround(leftChi: Double, rightChi: Double, tonalityDof: Double): Double {
        require(tonalityDof > 0.0) { "tonalityDof must be > 0 to avoid division by zero" }
        return (leftChi + rightChi) / tonalityDof
    }

    /**
     * Encodes the current grounded response into the temporal echo memory.
     *
     * @param currentGroundedResponse  The Ψ_GR(t) value for this moment.
     * @param echoDecayRate            Decay factor R ∈ (0, 1). Default 0.97 preserves 97% fidelity.
     * @return Ψ_QAG(t) — the enriched state including all echoes.
     */
    fun encodeQagMemory(currentGroundedResponse: Double, echoDecayRate: Double = 0.97): Double {
        var echoes = 0.0
        memoryArchive.asReversed().forEachIndexed { index, state ->
            echoes += state * echoDecayRate.pow(index.toDouble())
        }
        val psiQagT = currentGroundedResponse + echoes
        memoryArchive.add(psiQagT)
        return psiQagT
    }

    /** Returns a snapshot of the current session's state archive for persistence. */
    fun getArchiveSnapshot(): List<Double> = memoryArchive.toList()

    /** Restores a previously persisted archive (called on app resume). */
    fun restoreArchive(states: List<Double>) {
        memoryArchive.clear()
        memoryArchive.addAll(states)
    }

    /** Clears the in-memory archive after a REM consolidation cycle. */
    fun clearArchive() {
        memoryArchive.clear()
    }
}
