package com.example.tada_nfc.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AdItem(
    val id: String,
    val title: String,
    val description: String,
    val image: String,
    val action: AdAction
)

@Serializable
data class AdAction(
    val type: String,
    val value: String
)

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val ADS_JSON_KEY = "ads_json"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var cachedAds: List<AdItem> = emptyList()

    fun init() {
        try {
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(mapOf(ADS_JSON_KEY to "[]"))
            
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val jsonString = remoteConfig.getString(ADS_JSON_KEY)
                    updateCachedAds(jsonString)
                    Log.d(TAG, "Remote Config fetch successful")
                } else {
                    Log.e(TAG, "Remote Config fetch failed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase init error: ${e.message}")
        }
    }

    private fun updateCachedAds(jsonString: String) {
        cachedAds = try {
            if (jsonString.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<AdItem>>(jsonString)
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error: ${e.message}")
            emptyList()
        }
    }

    fun fetchAds(context: Context, onResult: (List<AdItem>) -> Unit) {
        try {
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                val jsonString = if (task.isSuccessful) {
                    remoteConfig.getString(ADS_JSON_KEY)
                } else {
                    remoteConfig.getString(ADS_JSON_KEY) // Use current (possibly default or previously fetched) values
                }
                updateCachedAds(jsonString)
                onResult(cachedAds)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAds error: ${e.message}")
            onResult(emptyList())
        }
    }

    fun getCachedAds(context: Context): List<AdItem> {
        return try {
            if (cachedAds.isEmpty()) {
                val jsonString = Firebase.remoteConfig.getString(ADS_JSON_KEY)
                updateCachedAds(jsonString)
            }
            cachedAds
        } catch (e: Exception) {
            Log.e(TAG, "getCachedAds error: ${e.message}")
            emptyList()
        }
    }
}
