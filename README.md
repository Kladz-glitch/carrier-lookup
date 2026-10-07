<div align="center">

# 📱 Carrier Lookup

**Scopri l'operatore, il tipo di linea e il paese di qualsiasi numero di telefono.**

App Android moderna in Kotlin e Jetpack Compose, con controllo anti spam e blocco delle chiamate tramite Hiya.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/licenza-MIT-green)

*Fatta da **Clacson***

</div>

---

## 📥 Scarica l'app

Non serve Android Studio: scarica l'APK e installalo sul telefono.

**👉 [Scarica CarrierLookup.apk](https://github.com/Kladz-glitch/carrier-lookup/raw/main/download/CarrierLookup.apk)**

1. Apri il link dal telefono e scarica il file.
2. Aprilo. Android chiederà di consentire l'installazione da questa fonte: accetta.
3. Premi **Installa**.

Serve Android 8.0 o superiore. Se Android avvisa che l'app non viene dal Play Store, è normale: è un'app indipendente.

## ✨ Cosa fa

| | |
|---|---|
| 🔎 **Ricerca numeri** | Scegli il paese, scrivi il numero e ottieni operatore, tipo di linea (mobile, fisso, VoIP…), paese e validità. Puoi incollare un numero o sceglierlo dalla rubrica. |
| 🌐 **Online + offline** | Usa l'API di omkar.cloud per dati reali; se la rete non c'è o i crediti finiscono, ripiega su libphonenumber di Google. |
| 🛡️ **Anti spam** | Ogni risultato ha un livello di rischio (basso, medio, alto) con i motivi. Per bloccare le chiamate spam l'app guida all'uso di **Hiya**. |
| 📊 **Contatore crediti** | Quante ricerche online hai usato oggi e nel mese, con data e ora dell'ultima. |
| 🕘 **Cronologia** | Le ultime ricerche restano salvate sul telefono. |
| 📡 **Offerte operatori** | Scorciatoie ai siti di TIM, Vodafone, WindTre, Iliad e Fastweb. |
| 🌗 **Tema e lingua** | Tema scuro o chiaro, interfaccia in italiano e inglese. |
| 👋 **Accesso con Google** | Saluto personalizzato in base all'ora del giorno. |

## 🧰 Tecnologie

- **Kotlin** e **Jetpack Compose** con **Material 3**
- **libphonenumber** (con moduli carrier e geocoder) per la ricerca offline
- **Credential Manager** per l'accesso con Google
- **Hiya** (app esterna) per il blocco delle chiamate spam
- API [omkar.cloud Phone Lookup](https://www.omkar.cloud) per la ricerca online

## 🚀 Come compilarla

1. Clona il repository e aprilo con **Android Studio**.
2. Nel file `local.properties` (nella cartella principale) aggiungi:
   ```properties
   omkar.apiKey=LA_TUA_CHIAVE
   google.webClientId=IL_TUO_CLIENT_ID.apps.googleusercontent.com
   ```
   Trovi un esempio in `local.properties.example`. Il file `local.properties` non viene mai caricato su GitHub.
3. Premi ▶ con un telefono collegato.

**Chiave API.** Si ottiene gratis su [omkar.cloud](https://www.omkar.cloud). Senza chiave l'app funziona solo in modalità offline. L'APK scaricabile da questa pagina ha già la chiave dell'autore, condivisa da tutti (200 ricerche al mese in tutto): se finisce, l'app passa alla ricerca offline.

**Accesso con Google.** Nell'APK scaricabile funziona solo per gli account autorizzati nel progetto Google dell'autore. Se compili tu l'app, nella Google Cloud Console servono un client OAuth di tipo *Android* (con il nome del pacchetto `com.example.carrierlookup` e lo SHA-1 della tua chiave) e uno di tipo *Applicazione web*. Il Client ID del secondo va in `local.properties`. Senza, l'app funziona lo stesso ma senza accesso.

**Loghi degli operatori.** Sono in `app/src/main/res/drawable/` (`logo_tim.png`, `logo_vodafone.png`, `logo_windtre.png`, `logo_iliad.png`, `logo_fastweb.png`). Sono marchi dei rispettivi proprietari, usati solo per indicare l'operatore. Se manca un file, l'app mostra un cerchio colorato con l'iniziale.

## ⚠️ Cose da sapere

- Con la **portabilità del numero** (MNP) l'operatore mostrato può non essere quello attuale.
- Il «Rischio spam» sotto ogni ricerca è una stima basata su regole (tipo di numero, prefissi usati nelle truffe).
- Il blocco delle chiamate lo fa **Hiya**: va installato a parte e scelto come app anti spam nelle impostazioni di Android. Hiya non offre un'API pubblica, quindi l'app non può interrogare il suo database dei numeri.
- La chiave API dentro l'APK può essere estratta da chi lo smonta. Per un uso serio compila con una chiave tua.

## ☕ Sostieni il progetto

Se l'app ti è utile puoi offrirmi un caffè: **[paypal.me/claudiorubin](https://paypal.me/claudiorubin)**

## 📄 Licenza

Rilasciata con licenza **MIT**, vedi il file [LICENSE](LICENSE).

---

<details>
<summary><b>🇬🇧 English summary</b></summary>

**Carrier Lookup** is an Android app (Kotlin + Jetpack Compose, Material 3) that finds the carrier, line type and country of a phone number. It uses the omkar.cloud API online and Google's libphonenumber offline, and includes a rule-based spam risk check, a guide to block spam calls with Hiya, search history, credit counter, dark/light theme and Italian/English UI.

To build it, open the project in Android Studio, set `omkar.apiKey` and `google.webClientId` in `local.properties` (see `local.properties.example`) and press Run. The downloadable APK already includes the author's shared API key (200 online searches per month in total, then it falls back to offline). Operator logos are trademarks of their respective owners and are used only to identify the carrier.

Released under the MIT license. Made by Clacson.

</details>
