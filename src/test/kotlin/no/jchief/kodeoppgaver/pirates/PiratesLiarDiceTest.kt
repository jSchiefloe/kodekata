package no.jchief.kodeoppgaver.pirates

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class PiratesLiarDiceTest {


    @Test
    fun jensTest() {
        val jens = Spiller.Jens()
        jens.oppdaterSvar(0,51)

        println(jens.svar.toString())
        assertTrue(jens.svar is Svar.MedVerdi.Raise)
        assertEquals(51, (jens.svar as Svar.MedVerdi.Raise).verdi)
    }

}