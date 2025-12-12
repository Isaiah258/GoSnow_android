package com.gosnow.app.datasupabase

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 全局持有「当前登录用户」的昵称 + 头像，
 * 登录成功后拉一次，后面 Home / Settings / 雪圈都从这里读。
 */
object CurrentUserStore {

    // 用泛型显式指定成 CurrentUserProfile?，这样可以接受 null
    private val _profile = MutableStateFlow<CurrentUserProfile?>(null)
    val profile: StateFlow<CurrentUserProfile?> = _profile.asStateFlow()

    /**
     * 登录成功后调用：从后端拉（或创建）一条 Users 记录，填充到本地。
     */
    suspend fun refreshFromServer() {
        _profile.value = ProfileRepository.getOrCreateCurrentUserProfile()
    }

    /**
     * 本地更新（比如 EditProfile 改了昵称 / 头像，不想再打一次网络）。
     */
    fun updateLocalProfile(newName: String, newAvatarUrl: String?) {
        val current = _profile.value ?: return
        _profile.value = current.copy(
            userName = newName,
            avatarUrl = newAvatarUrl
        )
    }

    /**
     * 退出登录时调用。
     */
    fun clear() {
        _profile.value = null
    }
}
