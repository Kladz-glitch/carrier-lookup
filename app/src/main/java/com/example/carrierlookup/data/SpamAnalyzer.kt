package com.example.carrierlookup.data

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

enum class SpamLevel { LOW, MEDIUM, HIGH }

enum class SpamReason { BLOCKLIST, PREMIUM, VOIP, INVALID, WANGIRI_PREFIX }

data class SpamAssessment(val level: SpamLevel, val reasons: List<SpamReason>) {
    /** Vero se le regole consigliano di bloccare la chiamata (premium o prefisso truffa). */
    val suspicious: Boolean
        get() = SpamReason.PREMIUM in reasons || SpamReason.WANGIRI_PREFIX in reasons
}

/**
 * Giudizio sul rischio spam basato su regole: non esiste un database mondiale degli
 * spammer, quindi conta la tipologia del numero e la lista nera personale.
 */
object SpamAnalyzer {

    /** Prefissi esteri spesso citati per le chiamate-truffa "Wangiri" (squillo e riaggancio). */
    private val wangiriPrefixes = setOf(
        "216", "223", "224", "225", "226", "228", "229", "231", "232",
        "233", "234", "235", "244", "252", "269"
    )

    fun assess(e164: String, lineType: LineType, isValid: Boolean, inBlocklist: Boolean): SpamAssessment {
        val reasons = mutableListOf<SpamReason>()
        if (inBlocklist) reasons += SpamReason.BLOCKLIST
        if (lineType == LineType.PREMIUM_RATE) reasons += SpamReason.PREMIUM
        if (lineType == LineType.VOIP) reasons += SpamReason.VOIP
        if (!isValid) reasons += SpamReason.INVALID
        if (countryCodeOf(e164) in wangiriPrefixes) reasons += SpamReason.WANGIRI_PREFIX

        val level = when {
            SpamReason.BLOCKLIST in reasons || SpamReason.PREMIUM in reasons -> SpamLevel.HIGH
            reasons.isNotEmpty() -> SpamLevel.MEDIUM
            else -> SpamLevel.LOW
        }
        return SpamAssessment(level, reasons)
    }

    /** Giudizio per un numero in arrivo, usando solo i dati offline di libphonenumber. */
    fun assessIncoming(raw: String, store: SpamStore): SpamAssessment? {
        val util = PhoneNumberUtil.getInstance()
        val parsed = try {
            util.parse(raw, "IT")
        } catch (e: NumberParseException) {
            return null
        }
        val e164 = util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        val type = when (util.getNumberType(parsed)) {
            PhoneNumberUtil.PhoneNumberType.PREMIUM_RATE -> LineType.PREMIUM_RATE
            PhoneNumberUtil.PhoneNumberType.VOIP -> LineType.VOIP
            else -> LineType.UNKNOWN
        }
        return assess(e164, type, util.isValidNumber(parsed), store.isBlocked(e164))
    }

    /** Trasforma un numero digitato in formato +39… (senza prefisso, si assume l'Italia). */
    fun normalize(raw: String): String? {
        val util = PhoneNumberUtil.getInstance()
        val cleaned = raw.trim().let { if (it.startsWith("00")) "+" + it.drop(2) else it }
        return try {
            val parsed = util.parse(cleaned, "IT")
            if (!util.isPossibleNumber(parsed)) null
            else util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            null
        }
    }

    private fun countryCodeOf(e164: String): String? = try {
        PhoneNumberUtil.getInstance().parse(e164, null).countryCode.toString()
    } catch (e: NumberParseException) {
        null
    }
}
