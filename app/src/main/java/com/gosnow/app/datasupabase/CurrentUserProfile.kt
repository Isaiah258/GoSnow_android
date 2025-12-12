package com.gosnow.app.datasupabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 映射 Supabase public."Users" 表的一条记录
 */
@Serializable
data class CurrentUserProfile(
    val id: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("avatar_url")
    val avatarUrl: String? = null
)


