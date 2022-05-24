package com.example.pametni_paketnik.models

import java.util.*


data class ParcelLocker (
    var numberParcelLocker: String,
    var name:String,
    var description :String,
    var city  :String,
    var address  :String,
    var postal  :Int,
    var location:String,
    var owner:String,
    val id:String ) {}