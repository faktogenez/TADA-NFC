package com.bodayan.tada.firebase

import android.content.Context
import android.util.Log
import com.bodayan.tada.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.bodayan.tada.models.AdAction
import com.bodayan.tada.models.AdItem

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    private var cachedAds: List<AdItem> = emptyList()

    fun init() {
        // Firestore не требует специальной инициализации настроек как Remote Config
    }

    fun fetchAds(context: Context, onResult: (List<AdItem>) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("ads").document("coupang")
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val products = document.get("products") as? List<Map<String, Any>>
                        if (products != null) {
                            val productsList = products.map { map ->
                                val actionMap = map["action"] as? Map<String, String>
                                AdItem(
                                    id = map["id"] as? String ?: "",
                                    description = map["description"] as? String ?: "",
                                    discount = map["discount"] as? String ?: "",
                                    price = map["price"] as? String ?: "",
                                    imageUrl = map["imageUrl"] as? String ?: "",
                                    action = AdAction(
                                        type = actionMap?.get("type") ?: "url",
                                        value = actionMap?.get("value") ?: ""
                                    )
                                )
                            }
                            cachedAds = productsList
                            onResult(cachedAds)
                        } else {
                            onResult(emptyList())
                        }
                    } else {
                        onResult(emptyList())
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Firestore fetch error: ${e.message}")
                    onResult(emptyList())
                }
        } catch (e: Exception) {
            Log.e(TAG, "fetchAds exception: ${e.message}")
            onResult(emptyList())
        }
    }

    fun getCachedAds(context: Context): List<AdItem> {
        return cachedAds
    }
}
