package com.example.pametni_paketnik

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pametni_paketnik.matrixTSP.location
import com.google.gson.reflect.TypeToken


import com.google.gson.Gson


class CitysViewModel(val _app: Application) : AndroidViewModel(_app) {
    val app = _app as MyApplication
    private val _unlocks = MutableLiveData<MutableList<location>?>()
    private val _unlocks2 = MutableLiveData<MutableList<location>?>()
    val citys: LiveData<MutableList<location>?> = _unlocks
    val citysAll: LiveData<MutableList<location>?> = _unlocks2

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadCitys(x: String) {
        val gson = Gson()

        val locations = gson.fromJson<MutableList<location>>(
            x,
            object : TypeToken<MutableList<location?>?>() {}.type
        )
        _unlocks.value=locations;
        _unlocks2.value=locations;

    }


}

