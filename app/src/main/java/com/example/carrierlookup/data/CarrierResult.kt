package com.example.carrierlookup.data

/** Tipo di linea restituito dalla ricerca. */
enum class LineType(val label: String) {
    MOBILE("Mobile"),
    FIXED_LINE("Linea fissa"),
    FIXED_OR_MOBILE("Fisso o mobile"),
    VOIP("VoIP"),
    TOLL_FREE("Numero verde"),
    PREMIUM_RATE("Tariffa premium"),
    OTHER("Altro"),
    UNKNOWN("Sconosciuto")
}

/** Risultato di una ricerca, indipendente dalla fonte dati. */
data class CarrierResult(
    val e164: String,
    val internationalFormat: String,
    val country: String,
    val carrier: String?,
    val lineType: LineType,
    val isValid: Boolean,
    val sourceName: String,
    val note: String? = null,
    /** Codici rete mobile (MCC-MNC), quando la fonte li fornisce. */
    val mccMnc: String? = null,
    /** true se la ricerca ha consumato un credito (richiesta alla fonte online). */
    val usedCredit: Boolean = false,
    /** Istante (ms) della ricerca. */
    val searchedAt: Long = 0L
)

/** Errori "attesi" da mostrare all'utente in modo leggibile (es. numero non valido). */
class LookupException(message: String) : Exception(message)

/** La fonte non è utilizzabile in questo momento (rete, chiave, limite): si può ripiegare su un'altra. */
class SourceUnavailableException(message: String) : Exception(message)
