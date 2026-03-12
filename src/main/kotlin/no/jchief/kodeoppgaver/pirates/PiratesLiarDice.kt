package no.jchief.kodeoppgaver.pirates

import no.jchief.kodeoppgaver.pirates.Spiller.Jens
import no.jchief.kodeoppgaver.pirates.Spiller.Will
import java.util.*

class PiratesLiarDice(
    val terninger: Stack<Terning>
) {
    fun play() {
        var forrigeSpiller: Spiller = Will()
        var aktuellSpiller: Spiller = Jens()
        var rundeNr = 1

        while (true) {
            when (val svarFraForrigeRunde = forrigeSpiller.svar) {
                is Svar.MedVerdi -> {
                    if (svarFraForrigeRunde.verdi == 66) {
                        aktuellSpiller.svar = Svar.Call()
                        println("#$rundeNr: ${aktuellSpiller.navn}: Svarer på 66: Call")
                    } else {
                        val høyesteVerdi = terninger.taTo().høyesteVerdi()
                        aktuellSpiller.oppdaterSvar(svarFraForrigeRunde.verdi, høyesteVerdi)
                        println("#$rundeNr: ${aktuellSpiller.navn}: svar: ${aktuellSpiller.svar}")
                    }
                }

                is Svar.Call -> {
                    when (val svar = aktuellSpiller.svar) {
                        is Svar.MedVerdi.Bluff -> {
                            println("#$rundeNr: ${aktuellSpiller.navn}: Spill over. ${forrigeSpiller.navn} callet ${aktuellSpiller.navn} som svarte ${svar.verdi} og som var Bluff. ${forrigeSpiller.navn} vinner! ")
                            return
                        }

                        else -> {
                            println("#$rundeNr: ${aktuellSpiller.navn}: Spill over. ${forrigeSpiller.navn} callet ${aktuellSpiller.navn} som IKKE var Bluff. ${aktuellSpiller.navn} vinner! ")
                            return
                        }
                    }
                }
            }

            val tmpAktuell = forrigeSpiller
            forrigeSpiller = aktuellSpiller
            aktuellSpiller = tmpAktuell
            rundeNr++
        }

        error("ingen flere terninger. noe har gått galt?")
    }
}

sealed interface Spiller {
    val navn: String
    var svar: Svar
    fun oppdaterSvar(eksisterendeSvar: Int, terningVerdi: Int)

    class Jens(override var svar: Svar = Svar.MedVerdi.Raise(0), override val navn: String = "Jens") : Spiller {
        override fun oppdaterSvar(eksisterendeSvar: Int, terningVerdi: Int) {
            svar = if (eksisterendeSvar == 66)
                Svar.Call()
            else if (terningVerdi > eksisterendeSvar)
                Svar.MedVerdi.Raise(verdi = terningVerdi)
            else
                Svar.Call()
        }
    }

    class Will(override var svar: Svar = Svar.MedVerdi.Raise(0), override val navn: String = "Will") : Spiller {
        override fun oppdaterSvar(eksisterendeSvar: Int, terningVerdi: Int) {
            svar = if (eksisterendeSvar == 66)
                Svar.Call()
            else if (terningVerdi > eksisterendeSvar)
                Svar.MedVerdi.Raise(verdi = terningVerdi)
            else
                Svar.MedVerdi.Bluff(verdi = lagLøgnOver(eksisterendeSvar + 1))
        }
    }
}

sealed interface Svar {
    sealed interface MedVerdi {
        val verdi: Int

        class Raise(override val verdi: Int) : MedVerdi, Svar {
            override fun toString(): String = "Raise: $verdi"
        }

        class Bluff(override val verdi: Int) : MedVerdi, Svar {
            override fun toString(): String = "Bluff: $verdi"
        }
    }

    class Call : Svar {
        override fun toString(): String = "Call"
    }
}

data class Terning(val verdi: Int)

fun main() {
    PiratesLiarDice(
//        terninger = Terninger.RemoteTerninger("https://piratesliarsdice.ekstern.dev.nav.no/dice").get()
//        terninger = Terninger.RemoteTerninger("https://piratesliarsdice.ekstern.dev.nav.no/youwin").get()
        terninger = Terninger.RemoteTerninger("https://piratesliarsdice.ekstern.dev.nav.no/youlose").get()
//        terninger = Terninger.RemoteTerninger("https://piratesliarsdice.ekstern.dev.nav.no/onlysixes").get()
//        terninger = Terninger.LokaleTerninger(0).get()
//        terninger = Terninger.LokaleTerninger(1).get()
//        terninger = Terninger.LokaleTerninger(2).get()
//        terninger = Terninger.LokaleTerninger(3).get()
    ).play()
}

fun Stack<Terning>.taTo(): Pair<Terning, Terning> =
    if (size >= 2)
        Pair(this.pop(), this.pop())
    else
        error("ingen flere terninger. noe har gått galt?")


fun Pair<Terning, Terning>.høyesteVerdi(): Int =
    if (this.first.verdi > this.second.verdi)
        "${this.first.verdi}${this.second.verdi}".toInt()
    else
        "${this.second.verdi}${this.first.verdi}".toInt()


fun lagLøgnOver(minsteGyldigeBud: Int): Int {
    val siffer = minsteGyldigeBud.let {
        val tier = it / 10
        val ener = it % 10
        Pair(tier, ener)
    }
    val tilfeldigTierplass = (siffer.first..6).random()

    val tilfeldigEnerplass = if (tilfeldigTierplass == siffer.first)
        (siffer.second..6).random()
    else (1..6).random()

    return Pair(
        Terning(tilfeldigTierplass),
        Terning(tilfeldigEnerplass)
    ).høyesteVerdi()
}