package com.bodayan.tada.models

import kotlinx.serialization.Serializable

@Serializable
data class AdItem(
    val id: String = "",
    val description: String = "",
    val discount: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val action: AdAction = AdAction()
)

@Serializable
data class AdAction(
    val type: String = "none",
    val value: String = ""
)
