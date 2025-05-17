package com.example.safepath.auth

import net.openid.appauth.AuthState
import net.openid.appauth.TokenResponse

/**
 * Estado de autenticación simplificado
 */
data class AuthState(
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val lastTokenRefresh: Long? = null
)

/**
 * Datos del usuario obtenidos de Cognito
 */
data class UserProfile(
    val userId: String,
    val email: String,
    val name: String?,
    val emailVerified: Boolean
)