package com.example.carrierlookup.ui

import android.app.Activity
import android.app.role.RoleManager
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.carrierlookup.data.AuthCancelledException
import com.example.carrierlookup.data.AuthException
import com.example.carrierlookup.data.CarrierResult
import com.example.carrierlookup.data.Countries
import com.example.carrierlookup.data.ApiConfig
import com.example.carrierlookup.data.GoogleAuth
import com.example.carrierlookup.data.SpamAnalyzer
import com.example.carrierlookup.data.SpamLevel
import com.example.carrierlookup.data.splitInternationalNumber
import com.example.carrierlookup.ui.theme.AppColors
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAB_SEARCH = 0
private const val TAB_HISTORY = 1
private const val TAB_SETTINGS = 2
private const val TAB_OFFERS = 3
private const val TAB_SPAM = 4

@Composable
fun LookupScreen(viewModel: LookupViewModel) {
    val s = LocalStrings.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { GoogleAuth() }

    var tab by rememberSaveable { mutableStateOf(TAB_SEARCH) }
    var showUsage by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val openMenu: () -> Unit = { scope.launch { drawerState.open() } }

    val onSignIn: () -> Unit = {
        viewModel.onSignInStarted()
        scope.launch {
            try {
                viewModel.onSignedIn(auth.signIn(context))
            } catch (e: AuthCancelledException) {
                viewModel.onSignInFailed(null)
            } catch (e: AuthException) {
                viewModel.onSignInFailed(e.message)
            } catch (e: Exception) {
                viewModel.onSignInFailed("Accesso non riuscito. Riprova.")
            }
        }
    }
    // Esce subito in locale; la pulizia lato Google avviene dopo (max 3 secondi).
    val onSignOut: () -> Unit = {
        viewModel.onSignedOut()
        scope.launch { auth.signOut(context) }
    }

    if (showUsage) {
        UsageDialog(times = state.usageTimes, onDismiss = { showUsage = false })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = AppColors.Card) {
                Text(
                    "Carrier Lookup",
                    color = AppColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                )
                val drawerItems = listOf(
                    Triple(TAB_SEARCH, s.tabSearch, Icons.Filled.Search),
                    Triple(TAB_OFFERS, s.offersTitle, Icons.Filled.LocalOffer),
                    Triple(TAB_SPAM, s.spamTitle, Icons.Filled.Shield),
                    Triple(TAB_HISTORY, s.tabHistory, Icons.Filled.History),
                    Triple(TAB_SETTINGS, s.tabSettings, Icons.Filled.Settings)
                )
                drawerItems.forEach { (index, label, icon) ->
                    NavigationDrawerItem(
                        label = { Text(label) },
                        icon = { Icon(icon, null) },
                        selected = tab == index,
                        onClick = {
                            tab = index
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = AppColors.Accent,
                            selectedTextColor = Color.White,
                            selectedIconColor = Color.White,
                            unselectedTextColor = AppColors.TextPrimary,
                            unselectedIconColor = AppColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) {
    Scaffold(
        containerColor = AppColors.Background,
        bottomBar = {
            NavigationBar(containerColor = AppColors.Card) {
                val items = listOf(
                    Triple(TAB_SEARCH, s.tabSearch, Icons.Filled.Search),
                    Triple(TAB_HISTORY, s.tabHistory, Icons.Filled.History),
                    Triple(TAB_SETTINGS, s.tabSettings, Icons.Filled.Settings)
                )
                items.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AppColors.Accent,
                            indicatorColor = AppColors.Accent,
                            unselectedIconColor = AppColors.TextSecondary,
                            unselectedTextColor = AppColors.TextSecondary
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (tab) {
                TAB_SEARCH -> SearchTab(
                    state = state,
                    viewModel = viewModel,
                    onSignIn = onSignIn,
                    onOpenHistory = { tab = TAB_HISTORY },
                    onOpenSettings = { tab = TAB_SETTINGS },
                    onOpenUsage = { showUsage = true },
                    onMenu = openMenu
                )
                TAB_OFFERS -> OffersTab(onMenu = openMenu)
                TAB_SPAM -> SpamTab(state = state, viewModel = viewModel, onMenu = openMenu)
                TAB_HISTORY -> HistoryTab(
                    history = state.history,
                    onPick = {
                        viewModel.showFromHistory(it)
                        tab = TAB_SEARCH
                    },
                    onClear = viewModel::clearHistory,
                    onMenu = openMenu
                )
                else -> SettingsTab(
                    state = state,
                    onSignIn = onSignIn,
                    onSignOut = onSignOut,
                    onOpenUsage = { showUsage = true },
                    onDarkMode = viewModel::setDarkMode,
                    onLanguage = viewModel::setLanguage,
                    onMenu = openMenu
                )
            }
        }
    }
    }
}

// ---- Scheda Ricerca ---------------------------------------------------------

@Composable
private fun SearchTab(
    state: LookupUiState,
    viewModel: LookupViewModel,
    onSignIn: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUsage: () -> Unit,
    onMenu: () -> Unit
) {
    val s = LocalStrings.current
    val focus = LocalFocusManager.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val util = remember { PhoneNumberUtil.getInstance() }
    var showCountries by remember { mutableStateOf(false) }

    val country = Countries.byPrefix(state.countryCode)

    fun applyText(text: String) {
        val split = splitInternationalNumber(util, text)
        if (split != null) viewModel.setNumber(split.countryCode, split.national)
        else viewModel.setNumber(null, text)
    }

    val contactLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        val uri = res.data?.data
        if (res.resultCode == Activity.RESULT_OK && uri != null) {
            val number = runCatching {
                context.contentResolver.query(
                    uri,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null, null, null
                )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
            }.getOrNull()
            if (number != null) applyText(number)
        }
    }

    if (showCountries) {
        CountryDialog(
            current = state.countryCode,
            onPick = {
                viewModel.onCountryCodeChange(it)
                showCountries = false
            },
            onDismiss = { showCountries = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Header(
            userName = state.userName,
            isSigningIn = state.isSigningIn,
            onSignIn = onSignIn,
            onHistory = onOpenHistory,
            onSettings = onOpenSettings,
            onMenu = onMenu
        )

        if (state.authError != null) {
            Text(state.authError, color = AppColors.Red, style = MaterialTheme.typography.bodySmall)
        }

        // Paese
        AppCard(modifier = Modifier.clickable { showCountries = true }) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(country?.flag ?: "🌍", fontSize = 26.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(s.country, color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text(
                        "${country?.name ?: s.otherPrefix}  (+${state.countryCode})",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = AppColors.TextSecondary)
            }
        }

        // Numero + pulsante cerca
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.number,
                onValueChange = viewModel::onNumberChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(s.numberHint) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focus.clearFocus()
                    viewModel.search()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.Card,
                    unfocusedContainerColor = AppColors.Card,
                    focusedBorderColor = AppColors.Accent,
                    unfocusedBorderColor = AppColors.Border,
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary,
                    focusedPlaceholderColor = AppColors.TextSecondary,
                    unfocusedPlaceholderColor = AppColors.TextSecondary,
                    cursorColor = AppColors.Accent
                )
            )
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = {
                    focus.clearFocus()
                    viewModel.search()
                },
                enabled = !state.isLoading,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
                } else {
                    Icon(Icons.Filled.Search, s.search, tint = Color.White)
                }
            }
        }

        // Scorciatoie
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionChip(s.paste, Icons.Filled.ContentPaste) {
                val text = clipboard.getText()?.text.orEmpty()
                if (text.isNotBlank()) applyText(text)
            }
            ActionChip(s.contacts, Icons.Filled.Contacts) {
                contactLauncher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
            }
            ActionChip(s.tabHistory, Icons.Filled.History, onOpenHistory)
            ActionChip(s.clear, Icons.Filled.Delete) { viewModel.clearInput() }
        }

        // Risultato
        ResultArea(state = state, onToggleBlock = { e164, blocked ->
            if (blocked) viewModel.unblock(e164) else viewModel.block(e164)
        })

        // Crediti
        CreditsCard(times = state.usageTimes, onClick = onOpenUsage)

        Footer()
    }
}

@Composable
private fun Header(
    userName: String?,
    isSigningIn: Boolean,
    onSignIn: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onMenu: () -> Unit
) {
    val s = LocalStrings.current
    val greeting = remember(s) { s.greeting(java.time.LocalTime.now().hour) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        MenuButton(onMenu)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (userName != null) "$greeting, $userName" else greeting,
                    color = AppColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                WavingHand()
            }
            if (userName == null) {
                Text(
                    if (isSigningIn) s.signingIn else s.signInGoogle,
                    color = AppColors.Accent,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable(enabled = !isSigningIn, onClick = onSignIn)
                )
            } else {
                Text(s.subtitle, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ---- Risultato --------------------------------------------------------------

@Composable
private fun ResultArea(state: LookupUiState, onToggleBlock: (String, Boolean) -> Unit) {
    Column(Modifier.animateContentSize()) {
        val result = state.result
        when {
            state.error != null -> ErrorCard(state.error)
            result != null -> {
                val blocked = result.e164 in state.blocklist
                ResultCard(result, blocked) { onToggleBlock(result.e164, blocked) }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    AppCard {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Error, null, tint = AppColors.Red)
            Spacer(Modifier.width(12.dp))
            Text(message, color = AppColors.TextPrimary)
        }
    }
}

@Composable
private fun ResultCard(result: CarrierResult, blocked: Boolean, onToggleBlock: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    AppCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CarrierAvatar(result.carrier)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        result.carrier ?: s.unknownCarrier,
                        color = AppColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (result.isValid) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            null,
                            tint = if (result.isValid) AppColors.Green else AppColors.Red,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (result.isValid) s.validNumber else s.invalidNumber,
                            color = if (result.isValid) AppColors.Green else AppColors.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                IconButton(onClick = { clipboard.setText(AnnotatedString(result.internationalFormat)) }) {
                    Icon(Icons.Filled.ContentCopy, s.copyNumber, tint = AppColors.TextSecondary)
                }
            }

            Spacer(Modifier.height(8.dp))
            HLine()
            InfoRow(s.rowNumber, result.internationalFormat)
            HLine()
            InfoRow(s.rowCountry, result.country)
            HLine()
            InfoRow(s.rowLineType, s.lineType(result.lineType))
            if (result.mccMnc != null) {
                HLine()
                InfoRow("MCC-MNC", result.mccMnc)
            }
            HLine()
            InfoRow(s.rowSource, result.sourceName)

            val spam = remember(result, blocked) {
                SpamAnalyzer.assess(result.e164, result.lineType, result.isValid, blocked)
            }
            val spamColor = when (spam.level) {
                SpamLevel.LOW -> AppColors.Green
                SpamLevel.MEDIUM -> Color(0xFFF59E0B)
                SpamLevel.HIGH -> AppColors.Red
            }
            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(14.dp), color = spamColor.copy(alpha = 0.14f)) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, null, tint = spamColor, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${s.spamRisk}: ${s.level(spam.level)}",
                            color = spamColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        if (spam.reasons.isEmpty()) s.noSignals
                        else spam.reasons.joinToString(" · ") { s.reason(it) },
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onToggleBlock) {
                Icon(Icons.Filled.Block, null, tint = AppColors.Red, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (blocked) s.blockRemove else s.blockAdd, color = AppColors.Red)
            }

            if (result.note != null) {
                Spacer(Modifier.height(8.dp))
                Text(result.note, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${result.e164}")))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppColors.Border)
                ) {
                    Icon(Icons.Filled.Call, null, tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.call, color = AppColors.TextPrimary)
                }
                OutlinedButton(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${result.e164}")))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AppColors.Border)
                ) {
                    Icon(Icons.Filled.Sms, null, tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.sendSms, color = AppColors.TextPrimary)
                }
            }
        }
    }
}

// ---- Crediti ----------------------------------------------------------------

@Composable
private fun CreditsCard(times: List<Long>, onClick: () -> Unit) {
    val s = LocalStrings.current
    AppCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    s.creditsUsed,
                    color = AppColors.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.Info, null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CreditTile(s.today, countToday(times), Modifier.weight(1f))
                CreditTile(s.thisMonth, countThisMonth(times), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row {
                Text(s.lastSearch, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text(
                    lastSearchText(times, s),
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CreditTile(label: String, value: Int, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AppColors.CardHigh
    ) {
        Column(
            Modifier.padding(vertical = 16.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                color = AppColors.TextPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(label, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun UsageDialog(times: List<Long>, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val today = LocalDate.now()
    val days = (0..6).map { today.minusDays(it.toLong()) }
    val todayTimes = times.filter { dateOf(it) == today }.sorted()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Card,
        title = { Text(s.creditsUsed, color = AppColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    s.creditsInfo,
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(6.dp))
                Text("${s.todayCount}: ${todayTimes.size}", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    if (todayTimes.isEmpty()) s.noSearchToday
                    else todayTimes.joinToString(" · ") { timeText(it) },
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Text(s.last7Days, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                days.forEach { d ->
                    val n = times.count { dateOf(it) == d }
                    Row {
                        Text(
                            if (d == today) s.today else "%02d/%02d".format(d.dayOfMonth, d.monthValue),
                            color = AppColors.TextSecondary
                        )
                        Spacer(Modifier.weight(1f))
                        Text(n.toString(), color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.close) } }
    )
}

// ---- Scelta paese -----------------------------------------------------------

@Composable
private fun CountryDialog(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    var custom by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Card,
        title = { Text(s.chooseCountry, color = AppColors.TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = custom,
                    onValueChange = { custom = it.filter(Char::isDigit).take(4) },
                    label = { Text(s.otherPrefixHint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (custom.isNotEmpty()) onPick(custom) }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.height(320.dp)) {
                    items(Countries.all) { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (c.prefix == current) AppColors.CardHigh else Color.Transparent)
                                .clickable { onPick(c.prefix) }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(c.flag, fontSize = 22.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(c.name, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                            Text("+${c.prefix}", color = AppColors.TextSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (custom.isNotEmpty()) onPick(custom) else onDismiss() }) {
                Text(if (custom.isNotEmpty()) "${s.usePrefix} +$custom" else s.close)
            }
        }
    )
}

// ---- Scheda Cronologia ------------------------------------------------------

@Composable
private fun HistoryTab(history: List<CarrierResult>, onPick: (CarrierResult) -> Unit, onClear: () -> Unit, onMenu: () -> Unit) {
    val s = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MenuButton(onMenu)
            Text(
                s.historyTitle,
                color = AppColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) { Text(s.clearAll, color = AppColors.Red) }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (history.isEmpty()) {
            Text(s.noHistory, color = AppColors.TextSecondary)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history, key = { it.e164 }) { r ->
                    AppCard(modifier = Modifier.clickable { onPick(r) }) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            CarrierAvatar(r.carrier, size = 42)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.internationalFormat, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${r.carrier ?: s.unknownCarrier} · ${s.lineType(r.lineType)}",
                                    color = AppColors.TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (r.searchedAt > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(dayText(r.searchedAt), color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                                    Text(timeText(r.searchedAt), color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---- Scheda Impostazioni ----------------------------------------------------

@Composable
private fun SettingsTab(
    state: LookupUiState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onOpenUsage: () -> Unit,
    onDarkMode: (Boolean) -> Unit,
    onLanguage: (String) -> Unit,
    onMenu: () -> Unit
) {
    val s = LocalStrings.current
    var showInfo by remember { mutableStateOf(false) }
    if (showInfo) InfoDialog(onDismiss = { showInfo = false })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MenuButton(onMenu)
            Text(s.settingsTitle, color = AppColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AppColors.CardHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.userName != null) {
                            Text(
                                state.userName.take(1).uppercase(),
                                color = AppColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        } else {
                            Icon(Icons.Filled.Person, null, tint = AppColors.TextSecondary)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(s.account, color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text(
                            state.userName ?: s.notSignedIn,
                            color = AppColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (state.authError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.authError, color = AppColors.Red, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(14.dp))
                if (state.userName == null) {
                    Button(
                        onClick = onSignIn,
                        enabled = !state.isSigningIn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
                    ) {
                        Text(if (state.isSigningIn) s.signingIn else s.signInGoogle)
                    }
                } else {
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AppColors.Border)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = AppColors.Red, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.signOut, color = AppColors.Red)
                    }
                }
            }
        }

        CreditsCard(times = state.usageTimes, onClick = onOpenUsage)

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text(s.appearance, color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.darkMode, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.darkMode,
                        onCheckedChange = onDarkMode,
                        colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Accent)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(s.language, color = AppColors.TextSecondary, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OptionPill("Italiano", state.language == "it", Modifier.weight(1f)) { onLanguage("it") }
                    OptionPill("English", state.language == "en", Modifier.weight(1f)) { onLanguage("en") }
                }
            }
        }

        val settingsContext = LocalContext.current
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Coffee, null, tint = AppColors.TextPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(s.donateTitle, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(s.donateHint, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (ApiConfig.isDonationConfigured) {
                            runCatching {
                                settingsContext.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(ApiConfig.DONATION_URL))
                                )
                            }
                        } else {
                            android.widget.Toast.makeText(settingsContext, s.donateNotSet, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
                ) { Text(s.donateButton) }
            }
        }

        AppCard(modifier = Modifier.clickable { showInfo = true }) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, null, tint = AppColors.TextSecondary)
                Spacer(Modifier.width(12.dp))
                Text(s.infoAndCredits, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
            }
        }

        Footer()
    }
}

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Card,
        title = { Text(s.infoTitle, color = AppColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.infoMade, color = AppColors.TextPrimary)
                Text(
                    s.infoSources,
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    s.infoPortability,
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s.close) } }
    )
}

@Composable
private fun Footer() {
    val s = LocalStrings.current
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "1.0"
    }
    Text(
        "${s.madeBy} · v$version",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        textAlign = TextAlign.Center,
        color = AppColors.TextSecondary.copy(alpha = 0.7f),
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun MenuButton(onClick: () -> Unit) {
    val s = LocalStrings.current
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Menu, s.menu, tint = AppColors.TextPrimary)
    }
}

// ---- Offerte operatori ------------------------------------------------------

private class Mno(val name: String, val url: String, val color: Color, val logo: String)

private val mnos = listOf(
    Mno("TIM", "https://www.tim.it", Color(0xFF1F4FA8), "logo_tim"),
    Mno("Vodafone", "https://www.vodafone.it", Color(0xFFE5484D), "logo_vodafone"),
    Mno("WindTre", "https://www.windtre.it", Color(0xFFF76808), "logo_windtre"),
    Mno("Iliad", "https://www.iliad.it", Color(0xFFD6409F), "logo_iliad"),
    Mno("Fastweb", "https://www.fastweb.it", Color(0xFF8E4EC6), "logo_fastweb")
)

@Composable
private fun OffersTab(onMenu: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MenuButton(onMenu)
            Text(s.offersTitle, color = AppColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(s.offersNote, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)

        mnos.forEach { mno ->
            AppCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Se in res/drawable c'è il file del logo (es. logo_tim.png) lo mostra,
                    // altrimenti un cerchio colorato con l'iniziale.
                    val logoId = remember(mno.logo) {
                        context.resources.getIdentifier(mno.logo, "drawable", context.packageName)
                    }
                    if (logoId != 0) {
                        Image(
                            painter = painterResource(logoId),
                            contentDescription = mno.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(6.dp)
                        )
                    } else {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(mno.color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(mno.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(mno.name, color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(s.offersTagline, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mno.url)))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
                    ) {
                        Text(s.offersOpen)
                    }
                }
            }
        }
        Footer()
    }
}

// ---- Anti spam --------------------------------------------------------------

@Composable
private fun SpamTab(state: LookupUiState, viewModel: LookupViewModel, onMenu: () -> Unit) {
    val s = LocalStrings.current
    val context = LocalContext.current

    fun roleManager(): RoleManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) context.getSystemService(RoleManager::class.java) else null

    val roleAvailable = remember {
        roleManager()?.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) == true
    }
    fun isHeld(): Boolean = roleManager()?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true

    var active by remember { mutableStateOf(isHeld()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        active = isHeld()
    }
    LaunchedEffect(Unit) { viewModel.refreshSpam() }

    var input by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MenuButton(onMenu)
            Text(s.spamTitle, color = AppColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, null, tint = if (active) AppColors.Green else AppColors.TextSecondary)
                    Spacer(Modifier.width(10.dp))
                    Text(s.callFilter, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(
                        if (active) s.filterActive else s.filterInactive,
                        color = if (active) AppColors.Green else AppColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (!active) {
                    Spacer(Modifier.height(8.dp))
                    if (roleAvailable) {
                        Text(s.filterExplain, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                roleManager()?.let {
                                    launcher.launch(it.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
                        ) { Text(s.filterActivate) }
                    } else {
                        Text(s.filterUnavailable, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(14.dp))
                HLine()
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.blockSuspected, color = AppColors.TextPrimary)
                        Text(s.blockSuspectedHint, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = state.blockSuspected,
                        onCheckedChange = viewModel::setBlockSuspected,
                        colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Accent)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(s.blockedCalls, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
                    Text(
                        state.blockedCount.toString(),
                        color = AppColors.TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text(s.blocklistTitle, color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(20)
                            inputError = false
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(s.addNumberHint) },
                        singleLine = true,
                        isError = inputError,
                        supportingText = if (inputError) ({ Text(s.invalidNumberInput) }) else null,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (viewModel.blockRaw(input)) input = "" else inputError = true
                        })
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (viewModel.blockRaw(input)) input = "" else inputError = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)
                    ) { Text(s.add) }
                }
                Spacer(Modifier.height(8.dp))
                if (state.blocklist.isEmpty()) {
                    Text(s.blocklistEmpty, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                } else {
                    state.blocklist.forEach { number ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(number, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.unblock(number) }) {
                                Icon(Icons.Filled.Delete, s.blockRemove, tint = AppColors.Red)
                            }
                        }
                    }
                }
            }
        }

        Text(s.spamLimits, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodySmall)
        Footer()
    }
}
