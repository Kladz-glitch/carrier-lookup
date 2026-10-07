package com.example.carrierlookup.data

/**
 * Una fonte di dati per il carrier lookup.
 * Per usare un'altra fonte (API online, scraping...) basta implementare questa
 * interfaccia e passarla al ViewModel.
 */
interface CarrierSource {
    val name: String

    /** @throws LookupException se il numero non è valido o la fonte non risponde. */
    suspend fun lookup(countryCode: String, number: String): CarrierResult
}
