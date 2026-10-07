package com.example.carrierlookup.data

import android.content.Context

/** Impostazioni salvate sul telefono. */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("carrier_lookup_settings", Context.MODE_PRIVATE)

    /** Chiave API usata per le ricerche online (fissa, incorporata nell'app). */
    val apiKey: String
        get() = ApiConfig.DEFAULT_API_KEY

    /** Nome di chi ha fatto l'accesso con Google (vuoto = nessun accesso). */
    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "").orEmpty()
        set(value) {
            prefs.edit().putString(KEY_USER_NAME, value.trim()).apply()
        }

    /** Ultimo prefisso usato. */
    var lastCountryCode: String
        get() = prefs.getString(KEY_CC, "39").orEmpty().ifBlank { "39" }
        set(value) {
            prefs.edit().putString(KEY_CC, value).apply()
        }

    /** Tema scuro attivo (predefinito: sì). */
    var darkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK, value).apply()
        }

    /** Lingua dell'interfaccia: "it" o "en" (predefinita: quella del telefono). */
    var language: String
        get() = prefs.getString(KEY_LANG, null)
            ?: if (java.util.Locale.getDefault().language == "it") "it" else "en"
        set(value) {
            prefs.edit().putString(KEY_LANG, value).apply()
        }

    private companion object {
        const val KEY_DARK = "dark_mode"
        const val KEY_LANG = "language"

        const val KEY_USER_NAME = "user_name"
        const val KEY_CC = "last_cc"
    }
}
