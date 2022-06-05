package com.example.pametni_paketnik.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val id: String,
    var username: String,
    var accesstoken:String,
    var name:String,
    var lastname:String,
    var email:String
)