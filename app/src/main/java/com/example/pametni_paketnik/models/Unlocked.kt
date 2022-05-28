package com.example.pametni_paketnik.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


data class Unlocked (
    @Expose var idParcelLocker: String,
    var idUser:String?=null,
    @Expose var dateTime:Date,
    @Expose var opened: Boolean,
    @SerializedName("_id")
    @Expose val id:String) {}