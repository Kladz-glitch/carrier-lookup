package com.example.carrierlookup.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Cronologia delle ultime ricerche, salvata sul telefono. */
class HistoryStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("carrier_lookup_history", Context.MODE_PRIVATE)

    fun load(): List<CarrierResult> = try {
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            CarrierResult(
                e164 = o.getString("e164"),
                internationalFormat = o.getString("intl"),
                country = o.getString("country"),
                carrier = o.optString("carrier").ifBlank { null },
                lineType = runCatching { LineType.valueOf(o.getString("type")) }.getOrDefault(LineType.UNKNOWN),
                isValid = o.getBoolean("valid"),
                sourceName = o.getString("source"),
                note = o.optString("note").ifBlank { null },
                mccMnc = o.optString("mcc").ifBlank { null },
                usedCredit = o.optBoolean("used"),
                searchedAt = o.optLong("at")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun save(list: List<CarrierResult>) {
        val arr = JSONArray()
        list.forEach { r ->
            arr.put(
                JSONObject()
                    .put("e164", r.e164)
                    .put("intl", r.internationalFormat)
                    .put("country", r.country)
                    .put("carrier", r.carrier.orEmpty())
                    .put("type", r.lineType.name)
                    .put("valid", r.isValid)
                    .put("source", r.sourceName)
                    .put("note", r.note.orEmpty())
                    .put("mcc", r.mccMnc.orEmpty())
                    .put("used", r.usedCredit)
                    .put("at", r.searchedAt)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object {
        const val KEY = "items"
    }
}
