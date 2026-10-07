package com.example.carrierlookup.data

import com.example.carrierlookup.BuildConfig

/** Configurazione dell'app. Chiavi e Client ID arrivano da local.properties (vedi README). */
object ApiConfig {
    /** Chiave API omkar.cloud usata di default da tutti gli utenti dell'app. */
    val DEFAULT_API_KEY: String = BuildConfig.OMKAR_API_KEY

    /**
     * Client ID di tipo "Applicazione web" creato nella Google Cloud Console
     * (finisce con .apps.googleusercontent.com). Serve per l'accesso con Google.
     */
    val GOOGLE_WEB_CLIENT_ID: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    /**
     * Link per le donazioni (PayPal.me, Ko-fi, Buy Me a Coffee, GitHub Sponsors…).
     * Sostituisci il testo con il tuo link: finché è vuoto il pulsante avvisa che non è configurato.
     */
    const val DONATION_URL = "https://paypal.me/claudiorubin"

    val isDonationConfigured: Boolean
        get() = DONATION_URL.startsWith("http")

    val isGoogleConfigured: Boolean
        get() = GOOGLE_WEB_CLIENT_ID.endsWith(".apps.googleusercontent.com")
}
