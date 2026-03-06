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

        while (terninger.count() >= 2) {
            when (val svarFraForrigeRunde = forrigeSpiller.svar) {
                is Svar.Raise -> {
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
                    when ((aktuellSpiller.svar as Svar.Raise).erLøgn) {
                        true -> {
                            println("#$rundeNr: ${aktuellSpiller.navn}: Spill over. ${forrigeSpiller.navn} callet ${aktuellSpiller.navn} som svarte ${(aktuellSpiller.svar as Svar.Raise).verdi} og som var løgn. ${forrigeSpiller.navn} vinner! ")
                            return
                        }

                        false -> {
                            println("#$rundeNr: ${aktuellSpiller.navn}: Spill over. ${forrigeSpiller.navn} callet ${aktuellSpiller.navn} som IKKE var løgn. ${aktuellSpiller.navn} vinner! ")
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

    class Jens(override var svar: Svar = Svar.Raise(0), override val navn: String = "Jens") : Spiller {
        override fun oppdaterSvar(eksisterendeSvar: Int, terningVerdi: Int) {
            svar = if (eksisterendeSvar == 66)
                Svar.Call()
            else if (terningVerdi > eksisterendeSvar)
                Svar.Raise(verdi = terningVerdi)
            else
                Svar.Call()
        }
    }

    class Will(override var svar: Svar = Svar.Raise(0), override val navn: String = "Will") : Spiller {
        override fun oppdaterSvar(eksisterendeSvar: Int, terningVerdi: Int) {
            svar = if (eksisterendeSvar == 66)
                Svar.Call()
            else if (terningVerdi > eksisterendeSvar)
                Svar.Raise(verdi = terningVerdi)
            else
//                Svar.Raise(verdi = (eksisterendeSvar + 1..66).random(), erLøgn = true)
                Svar.Raise(verdi = lagLøgnOver(eksisterendeSvar + 1), erLøgn = true)
        }
    }
}

sealed interface Svar {
    class Raise(val verdi: Int, val erLøgn: Boolean = false) : Svar {
        override fun toString(): String = "Raise: $verdi, erLøgn: $erLøgn"
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


fun Stack<Terning>.taTo(): Pair<Terning, Terning> = Pair(this.pop(), this.pop())


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