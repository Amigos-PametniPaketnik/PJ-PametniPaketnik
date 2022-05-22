package com.example.pametni_paketnik.models

import java.util.*


class AccessPermission (
    var idParcelLocker: String,
    var idUser:String,
    var accessableFrom:Date,
    var accessableTo:Date,
    val id:String ) {}