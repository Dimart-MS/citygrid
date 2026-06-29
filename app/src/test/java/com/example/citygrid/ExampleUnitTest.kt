package com.example.citygrid

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testFormulaPorcentaje() {
        // Fórmula del examen: 100 - (distancia_medida / altura_total) * 100 (altura = 20 cm)
        val dist1 = 2.0 // 100 - (2.0 / 20.0) * 100 = 90%
        val pct1 = (100 - (dist1 / 20.0) * 100).coerceIn(0.0, 100.0).toInt()
        assertEquals(90, pct1)

        val dist2 = 10.0 // 100 - (10.0 / 20.0) * 100 = 50%
        val pct2 = (100 - (dist2 / 20.0) * 100).coerceIn(0.0, 100.0).toInt()
        assertEquals(50, pct2)

        val dist3 = 18.0 // 100 - (18.0 / 20.0) * 100 = 10%
        val pct3 = (100 - (dist3 / 20.0) * 100).coerceIn(0.0, 100.0).toInt()
        assertEquals(10, pct3)
    }
}