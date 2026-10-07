package com.example.carrierlookup

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.carrierlookup.data.CarrierSource
import com.example.carrierlookup.data.HistoryStore
import com.example.carrierlookup.data.OfflineCarrierSource
import com.example.carrierlookup.data.OmkarCarrierSource
import com.example.carrierlookup.data.SettingsStore
import com.example.carrierlookup.data.SmartCarrierSource
import com.example.carrierlookup.data.SpamStore
import com.example.carrierlookup.data.UsageStore
import com.example.carrierlookup.ui.LookupScreen
import com.example.carrierlookup.ui.LookupViewModel
import com.example.carrierlookup.ui.LocalStrings
import com.example.carrierlookup.ui.stringsFor
import com.example.carrierlookup.ui.theme.CarrierLookupTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {

    private val settings by lazy { SettingsStore(applicationContext) }
    private val usage by lazy { UsageStore(applicationContext) }
    private val history by lazy { HistoryStore(applicationContext) }
    private val spam by lazy { SpamStore(applicationContext) }

    // Usa la fonte online; se non risponde ripiega su quella offline.
    private val source: CarrierSource by lazy {
        SmartCarrierSource(
            offline = OfflineCarrierSource(),
            online = OmkarCarrierSource { settings.apiKey },
            hasKey = { settings.apiKey.isNotBlank() }
        )
    }

    private val viewModel: LookupViewModel by viewModels {
        LookupViewModel.Factory(source, settings, usage, history, spam)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val dark = state.darkMode

            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                    navigationBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                )
                onDispose { }
            }

            CompositionLocalProvider(LocalStrings provides stringsFor(state.language)) {
                CarrierLookupTheme(darkTheme = dark) {
                    LookupScreen(viewModel)
                }
            }
        }
    }
}
