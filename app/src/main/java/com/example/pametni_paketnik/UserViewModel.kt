package com.example.pametni_paketnik

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pametni_paketnik.data.model.LoggedInUser

class UserViewModel : ViewModel() {
    val user = MutableLiveData<LoggedInUser?>(null)

    fun userLoggedIn(loggedInUser: LoggedInUser) {
        user.value = loggedInUser
    }
}