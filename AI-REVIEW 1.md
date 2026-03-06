# Code Review: Pirates Liar's Dice

## Oversikt

Prosjektet er en Kotlin-implementasjon av Pirates Liar's Dice-spillet, bygget med Maven. Koden er fordelt over to hovedfiler (`PiratesLiarDice.kt` og `Terninger.kt`) pluss en testfil. Løsningen bruker sealed interfaces for å modellere spillere, svar og terningkilder, noe som er idiomatisk Kotlin.

---

## Det som er bra

### Bruk av sealed interfaces
Modelleringen med `sealed interface` for `Spiller`, `Svar`, og `Terninger` er god Kotlin-praksis. Det gir typesikkerhet og gjør `when`-uttrykk uttømmende uten `else`-gren.

### Separasjon av terningkilder
At `Terninger` er abstrahert til et interface med `LokaleTerninger` og `RemoteTerninger` gjør det mulig å teste med forutsigbare verdier og samtidig støtte API-kall. Dette er en fin designbeslutning.

### Lesbarhet
Norske variabel- og klassenavn gjør domenet tydelig, og koden er generelt lettlest. Extension-funksjonene `taTo()` og `høyesteVerdi()` gjør spillogikken kompakt.

---

## Problemer og forbedringsforslag

### 1. Ugyldig løgnverdi (Bug)

**Fil:** `PiratesLiarDice.kt:75`

Når Will ikke kan slå gjeldende bud, velges en tilfeldig verdi fra `(eksisterendeSvar + 1..66)`:

```kotlin
Svar.Raise(verdi = (eksisterendeSvar + 1..66).random(), erLøgn = true)
```

Denne rangen kan produsere verdier som ikke er gyldige terningkombinasjoner. Gyldige verdier er de der begge siffer er 1-6 og første siffer >= andre siffer: 11, 21, 22, 31, 32, 33, 41, 42, 43, 44, 51, 52, 53, 54, 55, 61, 62, 63, 64, 65, 66. Men koden kan generere f.eks. 37, 48, 59 osv., som er umulige å lage med to terninger.

**Forslag:** Definer en liste med gyldige verdier og filtrer basert på hva som er høyere enn gjeldende bud.

### 2. API-endepunkt er ikke konfigurerbart ved kjøretid

**Fil:** `PiratesLiarDice.kt:92-98`

Oppgaven krever at API-endepunktet skal være konfigurerbart uten å bygge på nytt. URL-en er for øyeblikket hardkodet (og utkommentert) i `main()`. En enkel løsning ville vært å lese fra en miljøvariabel eller kommandolinjeargument:

```kotlin
fun main(args: Array<String>) {
    val url = args.firstOrNull()
        ?: System.getenv("DICE_URL")
        ?: "https://piratesliarsdice.ekstern.dev.nav.no/dice"
    PiratesLiarDice(terninger = Terninger.RemoteTerninger(url).get()).play()
}
```

### 3. Feil mainClass i pom.xml

**Fil:** `pom.xml:68`

`mainClass` er satt til `MainKt`, men den faktiske klassen (generert fra `PiratesLiarDice.kt`) vil hete `no.jchief.kodeoppgaver.pirates.PiratesLiarDiceKt`. Spillet vil ikke kunne kjøres via `mvn exec:java` uten å fikse dette.

### 4. Utskriftsformat matcher ikke oppgavekravet

Oppgaven krever at alle bud vises i formatet `"💀Joakim | 22 | 42 | 66"`. Koden skriver i stedet ut hver runde for seg med mye tekst. Budene bør samles og skrives ut som en oppsummering til slutt.

### 5. Svært begrenset testdekning

**Fil:** `PiratesLiarDiceTest.kt`

Det finnes kun én test som verifiserer at `Jens.oppdaterSvar` gir en `Raise` når terningverdien er høyere enn eksisterende bud. Oppgaven ber om "comprehensive tests". Følgende mangler:

- Test for `Jens` som caller (terningverdi lavere enn eksisterende bud)
- Test for `Jens` som caller på 66
- Tester for `Will` sin strategi (raise, løgn, call på 66)
- Test for `høyesteVerdi()` (at riktig siffer kommer først)
- Integrasjonstester for hele `play()`-flyten med kjente terningverdier
- Test for at spillet håndterer ulike scenarioer korrekt (løgner som blir oppdaget, sannferdige bud som blir callet)

### 6. Navnekonflikt: `LokaleTerninger`

**Fil:** `Terninger.kt:13` og `Terninger.kt:35`

Det finnes både en nested class `Terninger.LokaleTerninger` og et top-level `object LokaleTerninger`. Disse har ulike formål — klassen implementerer `Terninger`-interfacet, mens objektet holder testdata. Dette er forvirrende og kan føre til importfeil. Testdataene kunne vært et companion object inne i klassen, eller fått et annet navn som `TerningTestSett`.

### 7. Bruk av legacy `Stack`-klassen

**Fil:** `PiratesLiarDice.kt:7`

`java.util.Stack` er en legacy-klasse som arver fra `Vector` og er synkronisert — unødvendig overhead her. En `ArrayDeque` eller `MutableList` med `removeLast()` ville vært mer idiomatisk Kotlin.

### 8. Ingen validering av API-respons

**Fil:** `Terninger.kt:18-28`

`RemoteTerninger` deserialiserer API-responsen uten å validere at terningverdiene er i intervallet 1-6, eller at det faktisk er nok terninger. Ugyldig data fra API-et vil gi uforutsigbare resultater i stedet for en tydelig feilmelding.

### 9. Spillogikk: første runde er implisitt

**Fil:** `PiratesLiarDice.kt:10`

Will starter med `Svar.Raise(0)` som en dummy-verdi. Dette betyr at Jens alltid starter og alltid raiser i første runde (alt er høyere enn 0). Logikken fungerer, men den er ikke intuitiv — det ser ut som Will har gitt et bud på 0, noe som ikke gir mening i spillets regler. En mer eksplisitt modellering av "første trekk" ville gjort koden klarere.

### 10. Mangler README

Oppgaven krever en README med instruksjoner for lokal oppsett og kjøring. Denne mangler.

---

## Sammendrag

| Kategori | Vurdering |
|---|---|
| Struktur og design | Bra bruk av sealed interfaces og separasjon av terningkilder |
| Korrekthet | Bug i generering av løgnverdier; ellers fungerende spilllogikk |
| Testdekning | Svak — kun 1 test, oppgaven ber om "comprehensive tests" |
| Oppgavekrav | Mangler konfigurerbar URL, riktig utskriftsformat, og README |
| Kotlin-idiomatikk | Stort sett bra, men `Stack` og navnekonflikt trekker ned |
| Kjørbarhet | Feil `mainClass` i pom.xml hindrer kjøring via Maven |

Koden viser en god forståelse av Kotlin og spillets grunnleggende logikk, men trenger arbeid på testdekning, oppfyllelse av oppgavekrav, og fiksen av løgnverdi-buggen for å være komplett.
