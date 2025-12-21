package com.gosnow.app.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gosnow.app.BuildConfig
import com.gosnow.app.data.update.AppUpdateNotice
import com.gosnow.app.data.update.UpdateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel : ViewModel() {

    private val _updateNotice = MutableStateFlow<AppUpdateNotice?>(null)
    val updateNotice: StateFlow<AppUpdateNotice?> = _updateNotice.asStateFlow()

    private var hasChecked = false

    fun checkForUpdates() {
        if (hasChecked) return
        hasChecked = true

        viewModelScope.launch {
            val notice = UpdateRepository.checkUpdate() ?: return@launch

            // 获取当前 App 的版本号 (需要在 build.gradle 中配置 versionCode)
            val currentVersionCode = BuildConfig.VERSION_CODE
            val latestBuild = notice.latestBuild ?: 0

            // 只有当云端版本号 > 当前版本号时，才显示弹窗
            if (latestBuild > currentVersionCode) {
                _updateNotice.value = notice
            }
        }
    }

    fun dismissUpdate() {
        if (_updateNotice.value?.isForce == false) {
            _updateNotice.value = null
        }
    }
}