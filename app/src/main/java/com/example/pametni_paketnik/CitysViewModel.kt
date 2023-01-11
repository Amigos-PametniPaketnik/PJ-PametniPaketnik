package com.example.pametni_paketnik

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pametni_paketnik.data.Result
import com.example.pametni_paketnik.matrixTSP.location
import com.example.pametni_paketnik.ui.login.LoggedInUserView
import com.example.pametni_paketnik.ui.login.LoginResult
import com.google.gson.reflect.TypeToken


import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import java.util.ArrayList


class CitysViewModel(val _app: Application) : AndroidViewModel(_app) {
    val app = _app as MyApplication
    private val _unlocks = MutableLiveData<MutableList<location>?>()
    private val _unlocks2 = MutableLiveData<MutableList<location>?>()
    val citys: LiveData<MutableList<location>?> = _unlocks
    val citysAll: LiveData<MutableList<location>?> = _unlocks2
    private val _road: MutableLiveData<Road> = MutableLiveData()
    public val road = _road

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
    fun loadRoad(waypoints : ArrayList<GeoPoint>, context: Context){
        viewModelScope.launch {
            val roadManager = OSRMRoadManager(context)
            road.value = try {
                roadManager.getRoad(waypoints)
            }
            catch (exception: Exception) {
                println(exception.message)
                return@launch
            }
        }
    }
}

