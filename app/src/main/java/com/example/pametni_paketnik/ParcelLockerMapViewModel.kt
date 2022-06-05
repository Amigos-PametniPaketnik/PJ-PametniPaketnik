package com.example.pametni_paketnik

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.pametni_paketnik.models.ParcelLocker
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ParcelLockerMapViewModel(val _app: Application): AndroidViewModel(_app) {
    //val app = _app as MyApplication
    private val _parcelLockers: MutableLiveData<MutableList<ParcelLocker>> = MutableLiveData<MutableList<ParcelLocker>>()
    val parcelLockers: LiveData<MutableList<ParcelLocker>>
        get() = _parcelLockers
    fun loadParcelLockers() {
        viewModelScope.launch {
            _parcelLockers.value = getParcelLockers()
        }
    }
    private suspend fun getParcelLockers(): MutableList<ParcelLocker> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                //val token = app.getAccessToken()
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
                //val userId = app.getLoggedInUser().id
                val userId = "628d0646400faf9774a152cb"

                val request = Request.Builder()
                    .url("http://snf-58216.vm.okeanos-global.grnet.gr:3001/api/parcel-lockers/"+userId)
                    //.addHeader("Authorization", "Bearer " + token)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.e("Response", "${response.body.toString()}")
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    Log.e("Received: ", responseData)
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    val parcellockers = gson.fromJson<MutableList<ParcelLocker>>(responseData, object: TypeToken<MutableList<ParcelLocker>>() {}.type)
                    Log.e("API Call", "Getting unlocks successfully!")
                    //return@use Result.Success(jsonObject)
                    return@withContext parcellockers
                }
            }
            catch (e: Throwable) {
                Log.e("API Call Error", e.message.toString())
                Log.e("API Call", "Getting unlocks unsuccessfully!")
                //return@withContext Result.Error(IOException(e.message))
                return@withContext mutableListOf<ParcelLocker>()
            }
        }
    }
}