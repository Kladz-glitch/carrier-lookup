package com.example.carrierlookup.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.withTimeoutOrNull

/** Problema di accesso con un messaggio leggibile. */
class AuthException(message: String) : Exception(message)

/** L'utente ha chiuso la finestra di accesso: non è un errore da mostrare. */
class AuthCancelledException : Exception()

/** Accesso con Google tramite Credential Manager. Serve il Context di un'Activity. */
class GoogleAuth {

    /** Restituisce il nome di battesimo (o il nome completo) dell'utente. */
    suspend fun signIn(context: Context): String {
        if (!ApiConfig.isGoogleConfigured) {
            throw AuthException("Accesso Google non ancora configurato (manca il Client ID).")
        }

        val option = GetSignInWithGoogleOption.Builder(ApiConfig.GOOGLE_WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val response = try {
            CredentialManager.create(context).getCredential(context, request)
        } catch (e: GetCredentialCancellationException) {
            throw AuthCancelledException()
        } catch (e: NoCredentialException) {
            throw AuthException("Nessun account Google trovato su questo telefono.")
        } catch (e: GetCredentialException) {
            throw AuthException("Accesso non riuscito. Riprova.")
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val google = try {
                GoogleIdTokenCredential.createFrom(credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                throw AuthException("Risposta di Google non valida.")
            }
            return (google.givenName ?: google.displayName).orEmpty().ifBlank { "amico" }
        }
        throw AuthException("Tipo di accesso non supportato.")
    }

    suspend fun signOut(context: Context) {
        // Può restare appeso: dopo 3 secondi si procede comunque con l'uscita locale.
        try {
            withTimeoutOrNull(3000) {
                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
            }
        } catch (e: Exception) {
            // Se non riesce, l'uscita locale basta comunque.
        }
    }
}
