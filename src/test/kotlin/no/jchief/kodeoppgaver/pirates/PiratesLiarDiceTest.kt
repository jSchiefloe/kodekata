package no.jchief.kodeoppgaver.pirates

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class PiratesLiarDiceTest {


    @Test
    fun jensTest() {
        val jens = Spiller.Jens()
        jens.oppdaterSvar(0,51)

        println(jens.svar.toString())
        assertTrue(jens.svar is Svar.Raise)
        (jens.svar as Svar.Raise).let {
            assertEquals(51, it.verdi)
            assertFalse(it.erLøgn)
        }
    }

}