package com.example.pametni_paketnik.models

import java.util.*


class ParcelLocker (
    var numberParcelLocker: String,
    var name:String,
    var description :String,
    var city  :String,
    var address  :String,
    var postal  :Int,
    var location:MutableList<String>,
    var owner:String,
    val id:String ) {}