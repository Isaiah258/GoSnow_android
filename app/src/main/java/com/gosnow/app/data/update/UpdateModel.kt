package com.gosnow.app.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppUpdateNotice(
    val id: String,
    val platform: String,
    @SerialName("is_active") val isActive: Boolean,
    val title: String,
    val message: String,
    @SerialName("banner_url") val bannerUrl: String?,
    @SerialName("appstore_url") val downloadUrl: String,
    @SerialName("is_force") val isForce: Boolean,
    @SerialName("min_build") val minBuild: Int?,
    @SerialName("latest_build") val latestBuild: Int?,
    @SerialName("created_at") val createdAt: String
)