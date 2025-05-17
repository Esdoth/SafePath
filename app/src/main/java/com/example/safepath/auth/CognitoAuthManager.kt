package com.example.safepath.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import net.openid.appauth.*
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Extensión para DataStore
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class CognitoAuthManager(private val context: Context) {
    // Configuración de Cognito
    private var authServiceConfig: AuthorizationServiceConfiguration? = null
    val authService = AuthorizationService(context)

    // Estado de autenticación
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState

    // Configuración de la aplicación
    private val clientId = "2hnkfpve2cdlufvoc97e5r8lu4"
    private val redirectUri = Uri.parse("com.example.safepath://callback")
    private val scopes = "openid email profile"

    // Claves para DataStore
    private companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    fun getAuthorizationRequest(): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            authServiceConfig!!,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri
        )
            .setScope(scopes)
            .build()
    }

    /**
     * Limpia recursos
     */
    fun dispose() {
        authService.dispose()
    }

    /**
     * Inicializa la configuración y restaura el estado previo
     */
    suspend fun initialize() {
        try {
            // 1. Obtener configuración de Cognito
            val issuerUri = Uri.parse("https://cognito-idp.us-east-1.amazonaws.com/us-east-1_g5pc8BzKf")
            authServiceConfig = suspendCancellableCoroutine { continuation ->
                AuthorizationServiceConfiguration.fetchFromIssuer(
                    issuerUri,
                    object : AuthorizationServiceConfiguration.RetrieveConfigurationCallback {
                        override fun onFetchConfigurationCompleted(
                            config: AuthorizationServiceConfiguration?,
                            ex: AuthorizationException?
                        ) {
                            if (ex != null || config == null) {
                                continuation.resumeWithException(ex ?: Exception("Configuración no disponible"))
                            } else {
                                continuation.resume(config)
                            }
                        }
                    }
                )
            }

            // 2. Restaurar estado desde persistencia
            restoreAuthState()

            Log.d("CognitoAuth", "Manager inicializado correctamente")
        } catch (ex: Exception) {
            Log.e("CognitoAuth", "Error en inicialización: ${ex.message}")
            _authState.value = AuthState(error = "Error de inicialización")
        }
    }

    /**
     * Maneja la respuesta de autorización y obtiene tokens
     */
    suspend fun handleAuthorizationResponse(response: AuthorizationResponse): AuthState {
        _authState.value = _authState.value.copy(isLoading = true)

        return try {
            // 1. Intercambiar código por tokens
            val tokenRequest = response.createTokenExchangeRequest()
            val tokenResponse = suspendCancellableCoroutine { continuation ->
                authService.performTokenRequest(
                    tokenRequest,
                    ClientSecretPost(clientId), // Añade este parámetro
                    object : AuthorizationService.TokenResponseCallback {
                        override fun onTokenRequestCompleted(
                            response: TokenResponse?,
                            ex: AuthorizationException?
                        ) {
                            if (ex != null || response == null) {
                                continuation.resumeWithException(ex ?: Exception("Invalid token response"))
                            } else {
                                continuation.resume(response)
                            }
                        }
                    }
                )
            }

            // 2. Extraer información del usuario
            val idToken = parseIdToken(tokenResponse.idToken)
            val userProfile = UserProfile(
                userId = idToken.subject,
                email = idToken.email ?: "",
                name = idToken.name,
                emailVerified = idToken.emailVerified ?: false
            )

            // 3. Actualizar estado
            val newState = AuthState(
                isAuthenticated = true,
                isLoading = false,
                userProfile = userProfile,
                lastTokenRefresh = System.currentTimeMillis()
            )

            _authState.value = newState
            saveAuthState(tokenResponse, userProfile)
            newState
        } catch (ex: Exception) {
            val errorState = AuthState(
                isLoading = false,
                error = ex.message ?: "Error en autenticación"
            )
            _authState.value = errorState
            errorState
        }
    }

    /**
     * Cierra la sesión
     */
    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        _authState.value = AuthState()
    }

    // ... (mantén tus métodos existentes getAuthorizationRequest(), exchangeCodeForTokens(), dispose())

    /**
     * Parsea el ID Token para obtener claims
     */
    private fun parseIdToken(token: String?): IdToken {
        requireNotNull(token) { "ID Token no puede ser nulo" }
        return IdToken(token)
    }

    /**
     * Guarda el estado de autenticación
     */
    private suspend fun saveAuthState(tokenResponse: TokenResponse, userProfile: UserProfile) {
        context.dataStore.edit { preferences ->
            tokenResponse.accessToken?.let {
                preferences[ACCESS_TOKEN_KEY] = it
            }
            tokenResponse.refreshToken?.let {
                preferences[REFRESH_TOKEN_KEY] = it
            }
            preferences[USER_ID_KEY] = userProfile.userId
        }
    }

    /**
     * Restaura el estado de autenticación
     */
    private suspend fun restoreAuthState() {
        val preferences = context.dataStore.data.firstOrNull() ?: return

        val accessToken = preferences[ACCESS_TOKEN_KEY]
        val userId = preferences[USER_ID_KEY]

        if (accessToken != null && userId != null) {
            _authState.value = AuthState(
                isAuthenticated = true,
                userProfile = UserProfile(
                    userId = userId,
                    email = "", // Se puede cargar desde otro pref o dejarlo vacío
                    name = null,
                    emailVerified = false
                ),
                lastTokenRefresh = System.currentTimeMillis()
            )
        }
    }
}

/**
 * Clase para parsear el ID Token
 */
private class IdToken(token: String) {
    private val claims = parseToken(token)

    val subject: String get() = claims.getString("sub")
    val email: String? get() = claims.optString("email").takeIf { it.isNotEmpty() }
    val name: String? get() = claims.optString("name").takeIf { it.isNotEmpty() }
    val emailVerified: Boolean? get() = claims.optBoolean("email_verified").takeIf { claims.has("email_verified") }

    companion object {
        private fun parseToken(token: String): JSONObject {
            val parts = token.split(".")
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE), Charsets.UTF_8)
            return JSONObject(payload)
        }
    }
}