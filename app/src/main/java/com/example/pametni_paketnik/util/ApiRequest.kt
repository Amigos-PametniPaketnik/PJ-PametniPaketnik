package com.example.pametni_paketnik.util

import okhttp3.OkHttpClient
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ApiRequest {
    private val client = OkHttpClient()
    var token = "9ea96945-3a37-4638-a5d4-22e89fbc998f"

    fun post(url: String, payload: String): String {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + token)
            .post(payload.toRequestBody(MEDIA_TYPE_JSON))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            println(response.body!!.string())
            return response.body!!.string()
        }
    }

    companion object {
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}