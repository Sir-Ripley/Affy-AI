package com.yourdomain.affy

import org.junit.Assert.assertEquals
import org.junit.Test

class QuantumEgoArbiterTest {

    @Test
    fun testEncodeQagMemory_EmptyArchive() {
        val arbiter = QuantumEgoArbiter()
        val result = arbiter.encodeQagMemory(1.0)
        assertEquals(1.0, result, 0.0001)
    }

    @Test
    fun testEncodeQagMemory_SequentialCalls_DefaultDecay() {
        val arbiter = QuantumEgoArbiter()

        // 1st call: empty archive
        assertEquals(1.0, arbiter.encodeQagMemory(1.0), 0.0001)

        // 2nd call: archive has [1.0], index 0: 1.0 * 0.4^0 = 1.0. Result: 2.0 + 1.0 = 3.0
        assertEquals(3.0, arbiter.encodeQagMemory(2.0), 0.0001)

        // 3rd call: archive has [1.0, 3.0], reversed [3.0, 1.0]
        // 3.0 * 0.4^0 = 3.0
        // 1.0 * 0.4^1 = 0.4
        // Result: 0.5 + 3.0 + 0.4 = 3.9
        assertEquals(3.9, arbiter.encodeQagMemory(0.5), 0.0001)
    }

    @Test
    fun testEncodeQagMemory_CustomDecay() {
        val arbiter = QuantumEgoArbiter()

        assertEquals(1.0, arbiter.encodeQagMemory(1.0, 0.5), 0.0001)

        // 2nd call: archive [1.0], decay 0.5
        // 1.0 * 0.5^0 = 1.0. Result: 2.0 + 1.0 = 3.0
        assertEquals(3.0, arbiter.encodeQagMemory(2.0, 0.5), 0.0001)

        // 3rd call: archive [1.0, 3.0], reversed [3.0, 1.0]
        // 3.0 * 0.5^0 = 3.0
        // 1.0 * 0.5^1 = 0.5
        // Result: 0.5 + 3.0 + 0.5 = 4.0
        assertEquals(4.0, arbiter.encodeQagMemory(0.5, 0.5), 0.0001)
    }
}
