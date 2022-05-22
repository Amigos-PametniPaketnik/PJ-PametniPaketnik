package com.example.pametni_paketnik.models

import java.util.*


class Unlocked (
    var idParcelLocker: String,
    var idUser:String,
    var dateTime:Date,
    var opened: Boolean,
    val id:String = UUID.randomUUID().toString().replace("-", "")) {}