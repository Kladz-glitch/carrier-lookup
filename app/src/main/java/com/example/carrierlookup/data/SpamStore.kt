package com.example.carrierlookup.data

import android.content.Context

/** Lista nera personale e impostazioni del filtro anti spam, salvate sul telefono. */
class SpamStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("carrier_lookup_spam", Context.MODE_PRIVATE)

    /** Numeri bloccati, in formato internazionale (+39…). */
    fun blocklist(): List<String> =
        prefs.getStringSet(KEY_LIST, emptySet()).orEmpty().sorted()

    fun isBlocked(e164: String): Boolean = e164 in blocklist()

    fun add(e164: String) {
        val updated = HashSet(prefs.getStringSet(KEY_LIST, emptySet()).orEmpty())
        updated.add(e164)
        prefs.edit().putStringSet(KEY_LIST, updated).apply()
    }

    fun remove(e164: String) {
        val updated = HashSet(prefs.getStringSet(KEY_LIST, emptySet()).orEmpty())
        updated.remove(e164)
        prefs.edit().putStringSet(KEY_LIST, updated).apply()
    }

    /** Blocca anche i numeri sospetti secondo le regole (premium, prefissi truffa). */
    var blockSuspected: Boolean
        get() = prefs.getBoolean(KEY_SUSPECTED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SUSPECTED, value).apply()
        }

    var blockedCount: Int
        get() = prefs.getInt(KEY_COUNT, 0)
        private set(value) {
            prefs.edit().putInt(KEY_COUNT, value).apply()
        }

    fun incrementBlocked() {
        blockedCount = blockedCount + 1
    }

    private companion object {
        const val KEY_LIST = "blocklist"
        const val KEY_SUSPECTED = "block_suspected"
        const val KEY_COUNT = "blocked_count"
    }
}
