package com.example.carrierlookup.data

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Fonte offline basata su libphonenumber: nessuna rete, nessun limite di richieste.
 * Limite: restituisce l'operatore ORIGINALE in base al prefisso, quindi non vede
 * le portabilità (un numero passato da TIM a Vodafone risulta ancora TIM).
 */
class OfflineCarrierSource : CarrierSource {

    private val util: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
    private val carrierMapper: PhoneNumberToCarrierMapper by lazy { PhoneNumberToCarrierMapper.getInstance() }
    private val geocoder: PhoneNumberOfflineGeocoder by lazy { PhoneNumberOfflineGeocoder.getInstance() }

    override val name = "Offline (prefissi)"

    override suspend fun lookup(countryCode: String, number: String): CarrierResult =
        withContext(Dispatchers.Default) {
            val parsed = parsePhoneInput(util, countryCode, number)

            val locale = Locale.ITALIAN
            val carrier = carrierMapper.getNameForNumber(parsed, locale).ifBlank { null }
            val country = geocoder.getDescriptionForNumber(parsed, locale)
                .ifBlank { util.getRegionCodeForNumber(parsed)?.let { Locale("", it).getDisplayCountry(locale) } }
                ?: "Sconosciuto"

            val type = when (util.getNumberType(parsed)) {
                PhoneNumberType.MOBILE -> LineType.MOBILE
                PhoneNumberType.FIXED_LINE -> LineType.FIXED_LINE
                PhoneNumberType.FIXED_LINE_OR_MOBILE -> LineType.FIXED_OR_MOBILE
                PhoneNumberType.VOIP -> LineType.VOIP
                PhoneNumberType.TOLL_FREE -> LineType.TOLL_FREE
                PhoneNumberType.PREMIUM_RATE -> LineType.PREMIUM_RATE
                PhoneNumberType.UNKNOWN -> LineType.UNKNOWN
                else -> LineType.OTHER
            }

            CarrierResult(
                e164 = util.format(parsed, PhoneNumberFormat.E164),
                internationalFormat = util.format(parsed, PhoneNumberFormat.INTERNATIONAL),
                country = country,
                carrier = carrier,
                lineType = type,
                isValid = util.isValidNumber(parsed),
                sourceName = name,
                note = "Operatore originale in base al prefisso: non tiene conto della portabilità."
            )
        }
}
