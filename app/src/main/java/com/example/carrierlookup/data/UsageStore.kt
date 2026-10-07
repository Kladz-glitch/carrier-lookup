package com.example.carrierlookup.data

import android.content.Context

/**
 * Registro delle ricerche online ("crediti" usati), salvato sul telefono.
 * Conserva solo data e ora di ogni ricerca, per gli ultimi 60 giorni.
 */
class UsageStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("carrier_lookup_usage", Context.MODE_PRIVATE)

    /** Istanti (millisecondi) di tutte le ricerche online registrate. */
    fun all(): List<Long> =
        prefs.getString(KEY_TIMES, "").orEmpty()
            .split(',')
            .mapNotNull { it.toLongOrNull() }

    fun record(timeMillis: Long = System.currentTimeMillis()) {
        val limit = timeMillis - RETENTION_MILLIS
        val updated = (all() + timeMillis).filter { it >= limit }
        prefs.edit().putString(KEY_TIMES, updated.joinToString(",")).apply()
    }

    private companion object {
        const val KEY_TIMES = "online_lookup_times"
        const val RETENTION_MILLIS = 60L * 24 * 60 * 60 * 1000
    }
}
