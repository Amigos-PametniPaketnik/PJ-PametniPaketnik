package com.example.pametni_paketnik.data

import com.example.pametni_paketnik.data.model.LoggedInUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.*

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

                val payload =
                    """{"authtype" : "basic", "username" : "${username}", "password" : "${password}"}"""

                val request = Request.Builder()
                    .url("http://192.168.1.104:3001/api/users/authenticate")
                    .post(payload.toRequestBody(MEDIA_TYPE_JSON))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Error occured in API request. Code: $response")

                    val responseData = response.body!!.string()
                    val jsonObject = JSONObject(responseData)
                    if (jsonObject.getBoolean("success")) {
                        val user = LoggedInUser(jsonObject.getString("id"), jsonObject.getString("username"), jsonObject.getString("accesstoken"), jsonObject.getString("name"), jsonObject.getString("lastname"), jsonObject.getString("email"))
                        return@use Result.Success(user);
                    } else
                        return@use Result.Error(Exception("Error at login in login data source"))
                }
            } catch (e: Throwable) {
                return@withContext Result.Error(IOException("Error logging in", e))
            }
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}