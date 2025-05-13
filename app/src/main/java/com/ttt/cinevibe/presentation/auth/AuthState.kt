package com.ttt.cinevibe.presentation.auth

/**
 * Represents different states of an authentication operation (login, register, etc.)
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Represents different states of Firebase authentication operations
 */
sealed class FirebaseAuthState {
    object Idle : FirebaseAuthState()
    object Loading : FirebaseAuthState()
    object Success : FirebaseAuthState()
    data class Error(val message: String) : FirebaseAuthState()
}

/**
 * Represents different states of backend synchronization operations
 */
sealed class BackendSyncState {
    object Idle : BackendSyncState()
    object Loading : BackendSyncState()
    object Success : BackendSyncState()
    data class Error(val message: String) : BackendSyncState()
}
