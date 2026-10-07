<div align="center">

# 📱 Carrier Lookup

**Scopri l'operatore, il tipo di linea e il paese di qualsiasi numero di telefono.**

App Android moderna in Kotlin e Jetpack Compose, con filtro anti spam e blocco delle chiamate.

![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/licenza-MIT-green)

*Fatta da **Clacson***

</div>

---

## ✨ Cosa fa

| | |
|---|---|
| 🔎 **Ricerca numeri** | Scegli il paese, scrivi il numero e ottieni operatore, tipo di linea (mobile, fisso, VoIP…), paese e validità. Puoi incollare un numero o sceglierlo dalla rubrica. |
| 🌐 **Online + offline** | Usa l'API di omkar.cloud per dati reali; se la rete non c'è o i crediti finiscono, ripiega su libphonenumber di Google. |
| 🛡️ **Anti spam** | Ogni risultato ha un livello di rischio (basso, medio, alto) con i motivi. Lista nera personale e blocco delle chiamate in arrivo. |
| 📊 **Contatore crediti** | Quante ricerche online hai usato oggi e nel mese, con data e ora dell'ultima. |
| 🕘 **Cronologia** | Le ultime ricerche restano salvate sul telefono. |
| 📡 **Offerte operatori** | Scorciatoie ai siti di TIM, Vodafone, WindTre, Iliad e Fastweb. |
| 🌗 **Tema e lingua** | Tema scuro o chiaro, interfaccia in italiano e inglese. |
| 👋 **Accesso con Google** | Saluto personalizzato in base all'ora del giorno. |

## 🧰 Tecnologie

- **Kotlin** e **Jetpack Compose** con **Material 3**
- **libphonenumber** (con moduli carrier e geocoder) per la ricerca offline
- **Credential Manager** per l'accesso con Google
- **CallScreeningService** per il blocco delle chiamate
- API [omkar.cloud Phone Lookup](https://www.omkar.cloud) per la ricerca online

## 🚀 Come compilarla

1. Clona il repository e aprilo con **Android Studio**.
2. Premi ▶ con un telefono collegato. La chiave API e il Client ID di Google dell'app sono già inclusi, quindi funziona subito.
3. (Facoltativo) Per usare una chiave tua, aggiungi in `local.properties`:
   ```properties
   omkar.apiKey=LA_TUA_CHIAVE
   google.webClientId=IL_TUO_CLIENT_ID.apps.googleusercontent.com
   ```
   Trovi un esempio in `local.properties.example`. Il file `local.properties` non viene mai caricato su GitHub.

**Chiave API.** Quella inclusa è condivisa da tutti (200 ricerche al mese in tutto): se finisce, l'app passa alla ricerca offline. Per avere crediti tuoi, prendi una chiave gratuita su [omkar.cloud](https://www.omkar.cloud).

**Accesso con Google.** L'accesso funziona solo per gli account autorizzati nel progetto Google dell'app e per le build firmate con la chiave dell'autore. Se compili tu l'app con una chiave diversa, nella Google Cloud Console servono un client OAuth di tipo *Android* (con il nome del pacchetto `com.example.carrierlookup` e lo SHA-1 della tua chiave) e uno di tipo *Applicazione web*. Il Client ID del secondo va in `local.properties`. Senza, l'app funziona lo stesso ma senza accesso.

**Loghi degli operatori.** Sono in `app/src/main/res/drawable/` (`logo_tim.png`, `logo_vodafone.png`, `logo_windtre.png`, `logo_iliad.png`, `logo_fastweb.png`). Sono marchi dei rispettivi proprietari, usati solo per indicare l'operatore. Se manca un file, l'app mostra un cerchio colorato con l'iniziale.

## ⚠️ Cose da sapere

- Con la **portabilità del numero** (MNP) l'operatore mostrato può non essere quello attuale.
- Non esiste un elenco mondiale di spammer: il giudizio anti spam si basa su regole (tipo di numero, prefissi usati nelle truffe) e sulla tua lista nera.
- Il blocco delle chiamate funziona solo dopo aver scelto l'app come app anti spam nelle impostazioni di Android (da Android 10).
- La chiave API inclusa è pubblica: chiunque può usarla e consumarne i crediti. Per un uso serio metti una chiave tua.

## ☕ Sostieni il progetto

Se l'app ti è utile puoi offrirmi un caffè: **[paypal.me/claudiorubin](https://paypal.me/claudiorubin)**

## 📄 Licenza

Rilasciata con licenza **MIT**, vedi il file [LICENSE](LICENSE).

---

<details>
<summary><b>🇬🇧 English summary</b></summary>

**Carrier Lookup** is an Android app (Kotlin + Jetpack Compose, Material 3) that finds the carrier, line type and country of a phone number. It uses the omkar.cloud API online and Google's libphonenumber offline, and includes a rule-based spam risk check, a personal blocklist, incoming call blocking, search history, credit counter, dark/light theme and Italian/English UI.

To build it, just open the project in Android Studio and press Run: a shared API key and Google Client ID are already included (200 online searches per month in total, then it falls back to offline). To use your own, set `omkar.apiKey` and `google.webClientId` in `local.properties` (see `local.properties.example`). Google sign-in only works for builds signed with the author's key. Operator logos are trademarks of their respective owners and are used only to identify the carrier.

Released under the MIT license. Made by Clacson.

</details>
