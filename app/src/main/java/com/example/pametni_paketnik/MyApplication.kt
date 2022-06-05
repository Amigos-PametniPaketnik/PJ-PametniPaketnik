package com.example.pametni_paketnik

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.pametni_paketnik.data.model.LoggedInUser
import com.example.pametni_paketnik.ui.login.LoggedInUserView
import java.util.*

const val MY_SP_FILE_NAME = "myshared.data"

class MyApplication: Application() {
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        initShared()
        if (!containsID())
            saveID(UUID.randomUUID().toString().replace("-", ""))
    }
    fun initShared() {
        sharedPreferences = getSharedPreferences(MY_SP_FILE_NAME, Context.MODE_PRIVATE)
    }
    fun saveID(id: String) {
        with(sharedPreferences.edit()) {
            putString("ID", id)
            apply()
        }
    }
    fun containsID(): Boolean {
        return sharedPreferences.contains("ID")
    }
    fun getID(): String? {
        return sharedPreferences.getString("ID", "DefaultNoData")
    }
    fun saveLoggedInUser(loggedInUser: LoggedInUserView) {
        with(sharedPreferences.edit()) {
            putString("id", loggedInUser.id)
            putString("username", loggedInUser.username)
            putString("name", loggedInUser.name)
            putString("lastname", loggedInUser.lastname)
            putString("email", loggedInUser.email)
            putString("accesstoken", loggedInUser.accesstoken)
            apply()
        }
    }
    fun containsLoggedInUser(): Boolean {
        return sharedPreferences.contains("id") && sharedPreferences.contains("username") && sharedPreferences.contains("name")
                && sharedPreferences.contains("lastname") && sharedPreferences.contains("email") && sharedPreferences.contains("accesstoken")
    }
    fun getLoggedInUser(): LoggedInUserView {
        return LoggedInUserView(
            sharedPreferences.getString("id", "")!!,
            sharedPreferences.getString("username", "")!!,
            sharedPreferences.getString("accesstoken", "")!!,
            sharedPreferences.getString("name", "")!!,
            sharedPreferences.getString("lastname", "")!!,
            sharedPreferences.getString("email", "")!!
        )
    }
    fun removeLoggedInUser() {
        with(sharedPreferences.edit()) {
            remove("id")
            remove("username")
            remove("name")
            remove("accesstoken")
            remove("lastname")
            remove("email")
            remove("AccessToken")
            apply()
        }
    }
    fun saveAccessToken(accessToken: String) {
        with(sharedPreferences.edit()) {
            putString("AccessToken", accessToken)
            apply()
        }
    }
    fun getAccessToken(): String? {
        return sharedPreferences.getString("AccessToken", null)
    }
    fun saveUserID(userId: String) {
        with(sharedPreferences.edit()) {
            putString("UserID", userId)
            apply()
        }
    }
    fun getUserID(): String? {
        return sharedPreferences.getString("UserID", null)
    }
}