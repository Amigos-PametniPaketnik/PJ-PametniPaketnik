package com.example.pametni_paketnik.models

import java.util.*

data class User (
    var username: String,
    var accesstoken:String,
    var name:String,
    var lastname:String,
    var email:String,
    val id:String) {}