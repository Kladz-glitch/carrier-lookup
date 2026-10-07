package com.example.carrierlookup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carrierlookup.data.CarrierResult
import com.example.carrierlookup.data.CarrierSource
import com.example.carrierlookup.data.HistoryStore
import com.example.carrierlookup.data.LookupException
import com.example.carrierlookup.data.SettingsStore
import com.example.carrierlookup.data.SpamAnalyzer
import com.example.carrierlookup.data.SpamStore
import com.example.carrierlookup.data.UsageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LookupUiState(
    val countryCode: String = "39",
    val number: String = "",
    val isLoading: Boolean = false,
    val result: CarrierResult? = null,
    val error: String? = null,
    val history: List<CarrierResult> = emptyList(),
    /** Istanti (ms) delle ricerche online registrate negli ultimi 60 giorni. */
    val usageTimes: List<Long> = emptyList(),
    /** Nome di chi ha fatto l'accesso con Google, se presente. */
    val userName: String? = null,
    val isSigningIn: Boolean = false,
    val authError: String? = null,
    val darkMode: Boolean = true,
    val language: String = "it",
    val blocklist: List<String> = emptyList(),
    val blockedCount: Int = 0,
    val blockSuspected: Boolean = true
)

class LookupViewModel(
    private val source: CarrierSource,
    private val settings: SettingsStore,
    private val usage: UsageStore,
    private val historyStore: HistoryStore,
    private val spam: SpamStore
) : ViewModel() {

    private val _state = MutableStateFlow(
        LookupUiState(
            countryCode = settings.lastCountryCode,
            history = historyStore.load(),
            usageTimes = usage.all(),
            userName = settings.userName.ifBlank { null },
            darkMode = settings.darkMode,
            language = settings.language,
            blocklist = spam.blocklist(),
            blockedCount = spam.blockedCount,
            blockSuspected = spam.blockSuspected
        )
    )
    val state: StateFlow<LookupUiState> = _state.asStateFlow()

    /** Rilegge lista nera e contatore (il servizio chiamate li aggiorna in background). */
    fun refreshSpam() {
        _state.update { it.copy(blocklist = spam.blocklist(), blockedCount = spam.blockedCount, blockSuspected = spam.blockSuspected) }
    }

    fun setBlockSuspected(value: Boolean) {
        spam.blockSuspected = value
        _state.update { it.copy(blockSuspected = value) }
    }

    /** Aggiunge un numero digitato alla lista nera. Restituisce false se non è valido. */
    fun blockRaw(raw: String): Boolean {
        val e164 = SpamAnalyzer.normalize(raw) ?: return false
        spam.add(e164)
        refreshSpam()
        return true
    }

    fun block(e164: String) {
        spam.add(e164)
        refreshSpam()
    }

    fun unblock(e164: String) {
        spam.remove(e164)
        refreshSpam()
    }

    fun setDarkMode(value: Boolean) {
        settings.darkMode = value
        _state.update { it.copy(darkMode = value) }
    }

    fun setLanguage(value: String) {
        settings.language = value
        _state.update { it.copy(language = value) }
    }

    fun onSignInStarted() {
        _state.update { it.copy(isSigningIn = true, authError = null) }
    }

    fun onSignedIn(name: String) {
        settings.userName = name
        _state.update { it.copy(isSigningIn = false, userName = name, authError = null) }
    }

    fun onSignInFailed(message: String?) {
        _state.update { it.copy(isSigningIn = false, authError = message) }
    }

    fun onSignedOut() {
        settings.userName = ""
        _state.update { it.copy(userName = null, isSigningIn = false, authError = null) }
    }

    fun onCountryCodeChange(value: String) {
        val cc = value.filter { c -> c.isDigit() }.take(4)
        settings.lastCountryCode = cc
        _state.update { it.copy(countryCode = cc, error = null) }
    }

    fun onNumberChange(value: String) {
        _state.update { it.copy(number = value.filter { c -> c.isDigit() || c == ' ' || c == '+' }.take(20), error = null) }
    }

    /** Imposta prefisso e numero insieme (da incolla o rubrica). */
    fun setNumber(countryCode: String?, number: String) {
        if (countryCode != null) settings.lastCountryCode = countryCode
        _state.update {
            it.copy(
                countryCode = countryCode ?: it.countryCode,
                number = number.filter { c -> c.isDigit() || c == ' ' || c == '+' }.take(20),
                error = null
            )
        }
    }

    fun clearInput() {
        _state.update { it.copy(number = "", result = null, error = null) }
    }

    fun search() {
        val current = _state.value
        if (current.isLoading) return
        _state.update { it.copy(isLoading = true, error = null, result = null) }

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val result = source.lookup(current.countryCode, current.number).copy(searchedAt = now)
                if (result.usedCredit) usage.record(now)
                _state.update { s ->
                    val newHistory = (listOf(result) + s.history.filterNot { it.e164 == result.e164 }).take(30)
                    historyStore.save(newHistory)
                    s.copy(
                        isLoading = false,
                        result = result,
                        usageTimes = usage.all(),
                        history = newHistory
                    )
                }
            } catch (e: LookupException) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Qualcosa è andato storto. Riprova.") }
            }
        }
    }

    fun showFromHistory(result: CarrierResult) {
        _state.update { it.copy(result = result, error = null) }
    }

    fun clearHistory() {
        historyStore.save(emptyList())
        _state.update { it.copy(history = emptyList()) }
    }

    class Factory(
        private val source: CarrierSource,
        private val settings: SettingsStore,
        private val usage: UsageStore,
        private val history: HistoryStore,
        private val spam: SpamStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LookupViewModel(source, settings, usage, history, spam) as T
    }
}
