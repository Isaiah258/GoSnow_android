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

private const val MIN_PASSWORD_LENGTH = 6

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
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

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value.trim(), errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入邮箱或手机号") }
            return
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { it.copy(errorMessage = "请输入至少 ${MIN_PASSWORD_LENGTH} 位密码") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.login(email, password)
            } catch (e: AuthApiService.AuthApiException) {
                _uiState.update { it.copy(errorMessage = e.message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "登录失败，请稍后重试") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(password = "") }
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
