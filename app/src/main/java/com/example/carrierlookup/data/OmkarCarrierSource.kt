package com.example.carrierlookup.data

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Fonte online: API "Phone Lookup" di omkar.cloud (GET con chiave nell'header "API-Key").
 * Il piano gratuito ha un numero limitato di richieste al mese.
 */
class OmkarCarrierSource(private val apiKey: () -> String) : CarrierSource {

    private val util: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    override val name = "Online (omkar.cloud)"

    override suspend fun lookup(countryCode: String, number: String): CarrierResult =
        withContext(Dispatchers.IO) {
            val key = apiKey().trim()
            if (key.isEmpty()) throw SourceUnavailableException("chiave API mancante")

            val parsed = parsePhoneInput(util, countryCode, number)
            val e164 = util.format(parsed, PhoneNumberFormat.E164)

            val url = URL(ENDPOINT + "?phone=" + URLEncoder.encode(e164, "UTF-8"))
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "GET"
                conn.connectTimeout = 10_000
                conn.readTimeout = 15_000
                conn.setRequestProperty("API-Key", key)
                conn.setRequestProperty("Accept", "application/json")

                when (val status = conn.responseCode) {
                    200 -> Unit
                    401, 403 -> throw SourceUnavailableException("chiave API non valida")
                    429 -> throw SourceUnavailableException("limite di richieste raggiunto")
                    else -> throw SourceUnavailableException("errore del servizio ($status)")
                }

                val body = conn.inputStream.bufferedReader().use { it.readText() }
                toResult(body, parsed, e164)
            } catch (e: IOException) {
                throw SourceUnavailableException("nessuna connessione")
            } finally {
                conn.disconnect()
            }
        }

    private fun toResult(
        body: String,
        parsed: com.google.i18n.phonenumbers.Phonenumber.PhoneNumber,
        e164: String
    ): CarrierResult {
        val json = try {
            JSONObject(body)
        } catch (e: JSONException) {
            throw SourceUnavailableException("risposta non valida")
        }

        val carrier = json.optString("carrier").takeIf { it.isNotBlank() && it != "null" }

        val lineType = when (json.optString("line_type").lowercase(Locale.ROOT)) {
            "mobile" -> LineType.MOBILE
            "landline", "fixed_line" -> LineType.FIXED_LINE
            "voip" -> LineType.VOIP
            "" , "null" -> LineType.UNKNOWN
            else -> LineType.OTHER
        }

        val iso = json.optString("country_code").takeIf { it.isNotBlank() && it != "null" }
            ?: util.getRegionCodeForNumber(parsed)
        val country = iso
            ?.let { Locale("", it).getDisplayCountry(Locale.ITALIAN) }
            ?.takeIf { it.isNotBlank() }
            ?: "Sconosciuto"

        val mcc = json.optString("mobile_country_code").takeIf { it.isNotBlank() && it != "null" }
        val mnc = json.optString("mobile_network_code").takeIf { it.isNotBlank() && it != "null" }

        return CarrierResult(
            e164 = json.optString("phone_number").ifBlank { e164 },
            internationalFormat = util.format(parsed, PhoneNumberFormat.INTERNATIONAL),
            country = country,
            carrier = carrier,
            lineType = lineType,
            isValid = json.optBoolean("is_valid_number", util.isValidNumber(parsed)),
            sourceName = name,
            mccMnc = if (mcc != null && mnc != null) "$mcc-$mnc" else null,
            usedCredit = true
        )
    }

    private companion object {
        const val ENDPOINT = "https://carrier-lookup-api.omkar.cloud/lookup"
    }
}
