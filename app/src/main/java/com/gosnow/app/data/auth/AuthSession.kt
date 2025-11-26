package com.gosnow.app.data.auth

data class AuthSession(
    val accessToken: String,
    val refreshToken: String?,
    val userId: String,
    val email: String
)
