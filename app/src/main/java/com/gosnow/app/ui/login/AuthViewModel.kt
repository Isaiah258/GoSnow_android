package com.gosnow.app.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gosnow.app.data.auth.AuthRepository
import com.gosnow.app.datasupabase.SupabaseClientProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isSendingCode: Boolean = false,
    val codeSent: Boolean = false,
    val isVerifyingCode: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isCheckingSession: Boolean = true
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        viewModelScope.launch {
            val user = repository.currentUser().getOrNull()
            _uiState.update {
                it.copy(
                    isLoggedIn = user != null,
                    isCheckingSession = false,
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null, successMessage = null) }
    }

    fun onVerificationCodeChange(value: String) {
        _uiState.update { it.copy(verificationCode = value, errorMessage = null, successMessage = null) }
    }

    fun sendCode() {
        val normalizedPhone = normalizePhoneNumber(_uiState.value.phoneNumber)
        if (normalizedPhone == null) {
            _uiState.update { it.copy(errorMessage = "请输入有效的手机号（仅支持中国大陆 11 位手机号）") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingCode = true, errorMessage = null, successMessage = null) }
            repository.sendOtpToPhone(normalizedPhone)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            codeSent = true,
                            successMessage = "验证码已发送，请查收",
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            codeSent = false,
                            errorMessage = throwable.message ?: "验证码发送失败，请稍后重试"
                        )
                    }
                }
        }
    }

    fun verifyCodeAndLogin() {
        val normalizedPhone = normalizePhoneNumber(_uiState.value.phoneNumber)
        if (normalizedPhone == null) {
            _uiState.update { it.copy(errorMessage = "请输入有效的手机号") }
            return
        }

        val codeDigits = _uiState.value.verificationCode.filter { it.isDigit() }
        if (codeDigits.length != 6) {
            _uiState.update { it.copy(errorMessage = "请输入短信中的 6 位验证码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingCode = true, errorMessage = null, successMessage = null) }
            repository.verifyOtpAndLogin(normalizedPhone, codeDigits)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            isLoggedIn = true,
                            successMessage = "登录成功",
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isVerifyingCode = false,
                            isLoggedIn = false,
                            errorMessage = throwable.message ?: "登录失败，请稍后重试"
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
            _uiState.update {
                LoginUiState(
                    isCheckingSession = false
                )
            }
        }
    }

    private fun normalizePhoneNumber(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null

        val digitsOnly = trimmed.filter { it.isDigit() }

        return when {
            trimmed.startsWith("+") && digitsOnly.length in 10..15 -> trimmed
            digitsOnly.length == 11 -> "+86$digitsOnly"
            digitsOnly.length in 10..15 -> "+$digitsOnly"
            else -> null
        }
    }

    companion object {
        fun provideFactory(@Suppress("UNUSED_PARAMETER") context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = AuthRepository(SupabaseClientProvider.supabaseClient)
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(repository) as T
                }
            }
    }
}
