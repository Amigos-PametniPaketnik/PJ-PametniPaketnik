package com.example.pametni_paketnik

import com.example.pametni_paketnik.models.Unlocked

data class SaveUnlockResult(
    val success: Unlocked? = null,
    val error: String? = null
)
