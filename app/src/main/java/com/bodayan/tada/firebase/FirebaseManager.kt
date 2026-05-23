package com.bodayan.tada.firebase

import android.content.Context
import android.util.Log
import com.bodayan.tada.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.serialization.json.Json
import com.bodayan.tada.models.AdItem

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val ADS_JSON_KEY = "coupang_ads"

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var cachedAds: List<AdItem> = emptyList()

    fun init() {
        try {
            val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
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
