package com.example.pametni_paketnik.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val id: String,
    var username: String,
    var accesstoken:String,
    var name:String,
    var lastname:String,
    var email:String
)