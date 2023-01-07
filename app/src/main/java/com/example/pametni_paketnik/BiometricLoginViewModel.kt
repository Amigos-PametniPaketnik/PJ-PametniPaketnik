package com.example.pametni_paketnik

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pametni_paketnik.data.Result
import com.example.pametni_paketnik.data.model.LoggedInUser
import com.example.pametni_paketnik.models.Unlocked
import com.example.pametni_paketnik.ui.login.LoggedInUserView
import com.example.pametni_paketnik.ui.login.LoginFormState
import com.example.pametni_paketnik.ui.login.LoginResult
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
import org.json.JSONObject
import java.io.File
import java.io.IOException

class BiometricLoginViewModel(val _app: Application): AndroidViewModel(_app) {
    val app = _app as MyApplication
    private var _resultPostPhoto: MutableLiveData<Boolean?> = MutableLiveData<Boolean?>(null)
    val resultPostPhoto: LiveData<Boolean?>
        get() = _resultPostPhoto
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun authenticateWithPhoto(photo: File) {
        viewModelScope.launch {
            val result = try {
                postPhotoToServer(photo)
            } catch (e: Exception) {
                Log.e("API error", "${e.message}")
                Result.Error(Exception("Error when logging with face: ${e.message}"))
            }

            when(result) {
                is Result.Success -> {
                    _loginResult.value =
                        LoginResult(success = LoggedInUserView(result.data.id, result.data.username, result.data.accesstoken, result.data.name, result.data.lastname, result.data.email))
                }
                else -> {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                }
            }
        }
    }

    private suspend fun postPhotoToServer(photo: File): Result<LoggedInUser> {
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
                    .url("http://snf-59574.vm.okeanos-global.grnet.gr:3001/api/users/authenticate")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.e("Response", "${response.body.toString()}")
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    Log.e("Response JSON", responseData)
                    val jsonObject = JSONObject(responseData)
                    if (jsonObject.getBoolean("success")) {
                        val user = LoggedInUser(jsonObject.getString("id"), jsonObject.getString("username"), jsonObject.getString("accesstoken"), jsonObject.getString("name"), jsonObject.getString("lastname"), jsonObject.getString("email"))
                        return@use Result.Success(user);
                    } else
                        return@use Result.Error(java.lang.Exception("Error at login in login data source"))
                }
            }
            catch (e: Throwable) {
                Log.e("API call Error", "${e.message}")
                return@withContext Result.Error(IOException("Error logging in", e))
            }
        }
    }
}