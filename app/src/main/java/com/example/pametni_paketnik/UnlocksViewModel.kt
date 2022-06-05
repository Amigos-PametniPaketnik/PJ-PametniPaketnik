package com.example.pametni_paketnik

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.pametni_paketnik.data.Result
import com.example.pametni_paketnik.models.Unlocked
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class UnlocksViewModel(val _app: Application) : AndroidViewModel(_app) {
    val app = _app as MyApplication
    private val _unlocks = MutableLiveData<MutableList<Unlocked>?>()
    val unlocks: LiveData<MutableList<Unlocked>?> = _unlocks

    fun loadUnlocksForBox(boxID: String) {
        viewModelScope.launch {
            _unlocks.value = getUnlocksForBox(boxID)
        }
    }

    suspend fun getUnlocksForBox(boxID: String): MutableList<Unlocked> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val token = app.getAccessToken()
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

                val request = Request.Builder()
                    .url("http://snf-58216.vm.okeanos-global.grnet.gr:3001/api/unlocks/getByParcelLocker/"+boxID)
                    .addHeader("Authorization", "Bearer " + token)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.e("Response", "${response.body.toString()}")
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    Log.e("Received: ", responseData)
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    val unlocks = gson.fromJson<MutableList<Unlocked>>(responseData, object: TypeToken<MutableList<Unlocked>>() {}.type)
                    Log.e("API Call", "Getting unlocks successfully!")
                    //return@use Result.Success(jsonObject)
                    return@withContext unlocks
                }
            }
            catch (e: Throwable) {
                Log.e("API Call Error", e.message.toString())
                Log.e("API Call", "Getting unlocks unsuccessfully!")
                //return@withContext Result.Error(IOException(e.message))
                return@withContext mutableListOf<Unlocked>()
            }
        }
    }
}