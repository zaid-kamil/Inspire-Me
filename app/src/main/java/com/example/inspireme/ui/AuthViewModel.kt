package com.example.inspireme.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

enum class AuthState {
    AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
}

class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.UNAUTHENTICATED)
    val authState get() = _authState

    private val _authMsg = MutableLiveData<String>()
    val authMsg get() = _authMsg

    private fun signIn(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).addOnFailureListener {
            invalidAuth(it.message)
        }.addOnSuccessListener {
            _authState.value = AuthState.AUTHENTICATED
        }

    private fun signUp(username: String, email: String, password: String) =
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            _authState.value = AuthState.AUTHENTICATED
            // update user profile with username
            it.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(username).build()
            )
        }.addOnFailureListener {
            invalidAuth(it.message)
        }

    private fun invalidAuth(message: String?) {
        _authState.value = AuthState.INVALID_AUTHENTICATION
        _authMsg.value = message.toString()
    }

    // methods for fragment to call

    fun login(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            signIn(email, password)
        } else {
            invalidAuth("Please enter valid email and password")
        }
    }

    fun register(username: String, email: String, password: String) {
        if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            signUp(username, email, password)
        } else {
            invalidAuth("Please enter valid username, email and password")
        }
    }

    fun checkAuthentication() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.AUTHENTICATED
        }
    }
}

class AuthViewModelFactory(auth: FirebaseAuth) :
    ViewModelProvider.Factory {
    private val auth = auth
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(auth) as T
        }
        throw IllegalArgumentException("Unable to construct viewmodel")
    }
}