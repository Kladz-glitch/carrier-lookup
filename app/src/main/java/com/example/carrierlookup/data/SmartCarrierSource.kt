package com.example.carrierlookup.data

/**
 * Usa la fonte online quando c'è una chiave API; se la fonte online non è
 * disponibile (niente rete, limite raggiunto, chiave errata) ripiega su quella offline
 * e lo segnala nella scheda del risultato.
 */
class SmartCarrierSource(
    private val offline: CarrierSource,
    private val online: CarrierSource,
    private val hasKey: () -> Boolean
) : CarrierSource {

    override val name = "Online + offline"

    override suspend fun lookup(countryCode: String, number: String): CarrierResult {
        if (!hasKey()) return offline.lookup(countryCode, number)

        return try {
            online.lookup(countryCode, number)
        } catch (e: SourceUnavailableException) {
            val fallback = offline.lookup(countryCode, number)
            fallback.copy(
                note = "Fonte online non disponibile (${e.message}): mostro il risultato offline. " +
                    (fallback.note ?: "")
            )
        }
    }
}
