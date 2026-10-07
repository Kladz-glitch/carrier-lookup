# Carrier Lookup

App Android (Kotlin + Jetpack Compose) che scopre l'operatore, il tipo di linea e il paese di un
numero di telefono. Include cronologia, contatore dei crediti, filtro anti spam con blocco chiamate,
accesso con Google, tema scuro/chiaro e interfaccia in italiano e inglese.

Fatta da **Clacson**.

## Come funziona
- **Online**: [omkar.cloud Phone Lookup API](https://www.omkar.cloud) (1 credito a ricerca).
- **Offline**: libphonenumber di Google, usata se la fonte online non risponde.
- **Anti spam**: giudizio basato su regole (numeri premium, VoIP, prefissi usati nelle truffe) e su una lista nera personale.

## Come compilarla
1. Apri la cartella del progetto con Android Studio.
2. Copia `local.properties.example` nelle righe di `local.properties` e inserisci i tuoi valori:
   ```
   omkar.apiKey=...
   google.webClientId=...
   ```
3. (Facoltativo) Aggiungi i loghi degli operatori in `app/src/main/res/drawable/` come
   `logo_tim.png`, `logo_vodafone.png`, `logo_windtre.png`, `logo_iliad.png`, `logo_fastweb.png`.
   Sono marchi di terzi, quindi non sono inclusi nel repository.
4. Premi ▶.

Per l'accesso con Google servono un client OAuth Android (nome pacchetto `com.example.carrierlookup`
e SHA-1 della tua chiave) e un client Web nella Google Cloud Console.

## Sostieni il progetto
[PayPal](https://paypal.me/claudiorubin)

## Licenza
MIT, vedi `LICENSE`.
