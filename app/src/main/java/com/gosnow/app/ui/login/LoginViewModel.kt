package com.gosnow.app.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosnow.app.data.auth.AuthApiService
import com.gosnow.app.data.auth.AuthPreferences
import com.gosnow.app.data.auth.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_CODE_LENGTH = 4
private const val MIN_PHONE_LENGTH = 6

data class LoginUiState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isSendingCode: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val codeSent: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val isCheckingSession: Boolean = true
)

class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private var sessionJob: Job? = null

    init {
        observeSession()
    }

    private fun observeSession() {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            repository.session.collectLatest { session ->
                _uiState.update {
                    it.copy(
                        isLoggedIn = session != null,
                        errorMessage = null,
                        isCheckingSession = false
                    )
                }
            }
        }
    }

    fun onPhoneChange(value: String) {
        _uiState.update {
            it.copy(
                phoneNumber = value.trim(),
                errorMessage = null,
                codeSent = false
            )
        }
    }

    fun onVerificationCodeChange(value: String) {
        _uiState.update { it.copy(verificationCode = value.trim(), errorMessage = null) }
    }

    fun sendVerificationCode() {
        val phone = _uiState.value.phoneNumber

        if (phone.length < MIN_PHONE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "请输入有效的手机号") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true, errorMessage = null) }
            try {
                repository.sendSmsCode(phone)
                _uiState.update { it.copy(codeSent = true) }
            } catch (e: AuthApiService.AuthApiException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "验证码发送失败，请稍后重试"
                    )
                }
            }
            finally {
                _uiState.update { it.copy(isSendingCode = false) }
            }
        }
    }

    fun verifyCodeAndLogin() {
        val phone = _uiState.value.phoneNumber
        val code = _uiState.value.verificationCode

        if (phone.length < MIN_PHONE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "请输入有效的手机号") }
            return
        }

        if (code.length < MIN_CODE_LENGTH) {
            _uiState.update { it.copy(errorMessage = "请输入短信验证码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, errorMessage = null) }
            try {
                repository.loginWithSms(phone, code)
            } catch (e: AuthApiService.AuthApiException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "登录失败，请稍后重试") }
            } finally {
                _uiState.update { it.copy(isVerifyingCode = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update {
                it.copy(
                    phoneNumber = "",
                    verificationCode = "",
                    codeSent = false,
                    errorMessage = null
                )
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val preferences = AuthPreferences(context.applicationContext)
                    val repository = AuthRepository(
                        apiService = AuthApiService(),
                        preferences = preferences
                    )
                    @Suppress("UNCHECKED_CAST")
                    return LoginViewModel(repository) as T
                }
            }
    }
}
