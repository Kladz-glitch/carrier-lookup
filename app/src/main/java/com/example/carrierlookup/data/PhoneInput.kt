package com.example.carrierlookup.data

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

/** Trasforma prefisso + numero digitati dall'utente in un numero valido da interrogare. */
internal fun parsePhoneInput(
    util: PhoneNumberUtil,
    countryCode: String,
    number: String
): Phonenumber.PhoneNumber {
    val cc = countryCode.trim().removePrefix("+").filter { it.isDigit() }
    val digits = number.filter { it.isDigit() || it == '+' }

    if (cc.isEmpty()) throw LookupException("Inserisci il prefisso internazionale (es. 39).")
    if (digits.isEmpty()) throw LookupException("Inserisci un numero di telefono.")

    val raw = if (digits.startsWith("+")) digits else "+$cc$digits"

    return try {
        util.parse(raw, null)
    } catch (e: NumberParseException) {
        throw LookupException("Numero non riconosciuto. Controlla prefisso e cifre.")
    }
}

/** Numero separato in prefisso e cifre nazionali. */
data class SplitNumber(val countryCode: String, val national: String)

/** Prova a separare un numero scritto in forma internazionale ("+39 333…" o "0039 333…"). */
fun splitInternationalNumber(util: PhoneNumberUtil, raw: String): SplitNumber? {
    var text = raw.trim().filter { it.isDigit() || it == '+' }
    if (text.startsWith("00")) text = "+" + text.drop(2)
    if (!text.startsWith("+")) return null
    return try {
        val parsed = util.parse(text, null)
        SplitNumber(parsed.countryCode.toString(), util.getNationalSignificantNumber(parsed))
    } catch (e: NumberParseException) {
        null
    }
}
