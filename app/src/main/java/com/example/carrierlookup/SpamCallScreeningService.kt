package com.example.carrierlookup

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.carrierlookup.data.SpamAnalyzer
import com.example.carrierlookup.data.SpamLevel
import com.example.carrierlookup.data.SpamReason
import com.example.carrierlookup.data.SpamStore

/** Filtro chiamate: rifiuta i numeri della lista nera e, se attivo, quelli sospetti. */
class SpamCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val store = SpamStore(applicationContext)
        val incoming = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        val raw = callDetails.handle?.schemeSpecificPart

        var block = false
        if (incoming && !raw.isNullOrBlank()) {
            val assessment = SpamAnalyzer.assessIncoming(raw, store)
            if (assessment != null) {
                block = SpamReason.BLOCKLIST in assessment.reasons ||
                    (store.blockSuspected && (assessment.suspicious || assessment.level == SpamLevel.HIGH))
            }
        }

        val response = CallResponse.Builder()
        if (block) {
            response.setDisallowCall(true).setRejectCall(true).setSkipNotification(true)
            store.incrementBlocked()
        }
        respondToCall(callDetails, response.build())
    }
}
