package com.example.inspireme.models

// default values are required for Firebase models or else the app will crash
data class Post(
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val username: String = "",
    val uid: String = "",
)