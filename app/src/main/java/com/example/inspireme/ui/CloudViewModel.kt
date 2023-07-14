package com.example.inspireme.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.inspireme.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class PostUploadStatus { SUCCESS, FAILURE, IN_PROGRESS, NONE }
enum class PostLoadingStatus { SUCCESS, FAILURE, IN_PROGRESS, NONE }
class CloudViewModel(
    private val auth: FirebaseAuth, private val db: FirebaseFirestore
) : ViewModel() {

    private val _postUploadStatus = MutableLiveData<PostUploadStatus>(PostUploadStatus.NONE)
    val postUploadStatus get() = _postUploadStatus

    private val _postLoadingStatus = MutableLiveData<PostLoadingStatus>(PostLoadingStatus.NONE)
    val postLoadingStatus get() = _postLoadingStatus

    private val _authState = MutableLiveData<AuthState>(AuthState.UNAUTHENTICATED)
    val authState get() = _authState

    private val _msg = MutableLiveData<String>()
    val msg get() = _msg

    fun currentUser() = auth.currentUser
    fun signOut() = auth.signOut()
    fun getPosts() = db.collection("posts").get().addOnFailureListener {
        invalidTask(it.message)
    }.addOnSuccessListener {
        _postLoadingStatus.value = PostLoadingStatus.SUCCESS
        _msg.value = "Successfully loaded posts"
    }

    private fun uploadPost(post: Post) {
        _postUploadStatus.value = PostUploadStatus.IN_PROGRESS
        db.collection("posts").add(post).addOnFailureListener {
            invalidTask(it.message)
        }.addOnSuccessListener {
            _postUploadStatus.value = PostUploadStatus.SUCCESS
            _msg.value = "Successfully uploaded post"
        }
    }

    private fun invalidTask(message: String?) {
        _postLoadingStatus.value = PostLoadingStatus.FAILURE
        _msg.value = message.toString()
    }

    // methods for fragment to call
    fun addPost(title: String, description: String) {
        if (title.isNotEmpty() && description.isNotEmpty() && currentUser() != null) {
            uploadPost(
                Post(
                    title,
                    description,
                    username = currentUser()?.displayName!!,
                    uid = currentUser()?.uid!!
                )
            )
        } else {
            invalidTask("Please enter valid title and description")
        }
    }

    fun checkAuthentication() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.AUTHENTICATED
        }
    }
}

class CloudViewModelFactory(
    private val auth: FirebaseAuth, private val db: FirebaseFirestore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CloudViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return CloudViewModel(auth, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}