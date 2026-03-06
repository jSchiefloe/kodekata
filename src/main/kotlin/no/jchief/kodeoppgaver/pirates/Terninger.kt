package no.jchief.kodeoppgaver.pirates

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.jchief.kodeoppgaver.pirates.LokaleTerninger.sett
import java.net.URI
import java.util.*

sealed interface Terninger {

    fun get(): Stack<Terning>

    class LokaleTerninger(val settNr: Int) : Terninger {
        override fun get(): Stack<Terning> = sett[settNr]
    }

    class RemoteTerninger(val url: String) : Terninger {
        override fun get(): Stack<Terning> {
            val endepunktInnhold = URI.create(url).toURL()
                .readText(Charsets.UTF_8)

            val lista = Json.decodeFromString<List<EndepunktTerning>>(endepunktInnhold)

            val stack = Stack<Terning>()
            lista.map { Terning(it.value) }.forEach { stack.push(it) }

            return stack
        }

        @Serializable
        private data class EndepunktTerning(val name: String, val value: Int)
    }
}

object LokaleTerninger {
    val sett = listOf(
        Stack<Terning>().apply {
            push(Terning(1))
            push(Terning(5))
            push(Terning(2))
            push(Terning(3))
            push(Terning(6))
            push(Terning(4))
            push(Terning(4))
            push(Terning(1))
            push(Terning(1))
            push(Terning(2))
            push(Terning(2))
            push(Terning(2))
            push(Terning(1))
            push(Terning(1))
            push(Terning(1))
        },
        Stack<Terning>().apply {
            push(Terning(6))
            push(Terning(6))
            push(Terning(6))
            push(Terning(6))
        },
        Stack<Terning>().apply {
            push(Terning(1))
            push(Terning(2))
            push(Terning(3))
            push(Terning(4))
            push(Terning(5))
            push(Terning(6))
            push(Terning(1))
            push(Terning(2))
            push(Terning(3))
            push(Terning(4))
            push(Terning(5))
            push(Terning(6))
        }
    )
}