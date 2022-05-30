package com.example.pametni_paketnik

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pametni_paketnik.models.Unlocked
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.get
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class BiometricLoginViewModel(val _app: Application): AndroidViewModel(_app) {
    val app = _app as MyApplication
    private var _resultPostPhoto: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    val resultPostPhoto: LiveData<Boolean?>
    get() = _resultPostPhoto

    fun authenticateWithPhoto(photo: File) {
        viewModelScope.launch {
            try {
                _resultPostPhoto.value = postPhotoToServer(photo)
            } catch (e: Exception) {
                Log.e("API error", "${e.message}")
            }
        }
    }

    private suspend fun postPhotoToServer(photo: File): Boolean? {
        return withContext(Dispatchers.IO) {
            try {
                val compressedPhoto = Compressor.compress(app.applicationContext, photo) // Compress file with Compressor extension library for optimal HTTP POST Request
                val client = OkHttpClient()
                val token = app.getAccessToken()
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
                val MEDIA_TYPE_PNG = "image/png".toMediaType()

                val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("authtype", "biometric")
                    .addFormDataPart("photo", photo.name, compressedPhoto.asRequestBody(MEDIA_TYPE_PNG))
                    .build()

                val request = Request.Builder()
                    .url("http://192.168.1.104:3001/api/users/authenticate")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.e("Response", "${response.body.toString()}")
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    return@withContext true
                }
            }
            catch (e: Throwable) {
                Log.e("API Call Error", "${e.message}")
                return@withContext false
            }
        }
    }
}