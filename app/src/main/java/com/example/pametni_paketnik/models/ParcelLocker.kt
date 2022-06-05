package com.example.pametni_paketnik.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


class ParcelLocker (
    @Expose var numberParcelLocker: String,
    @Expose var name:String,
    @Expose var description :String,
    @Expose var city  :String,
    @Expose var address  :String,
    @Expose var postal  :Int,
    @Expose var location:MutableList<String>,
    @Expose var owner:String,
    @SerializedName("_id")
    @Expose val id:String ) {}