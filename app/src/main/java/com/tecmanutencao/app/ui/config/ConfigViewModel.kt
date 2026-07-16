package com.tecmanutencao.app.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.data.repository.EmpresaConfigRepository
import com.tecmanutencao.app.domain.model.EmpresaConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConfigUiState(
    val config: EmpresaConfig = EmpresaConfig(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ConfigViewModel(
    private val repository: EmpresaConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigUiState())
    val uiState: StateFlow<ConfigUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            repository.getConfig().collect { config ->
                _uiState.value = _uiState.value.copy(
                    config = config ?: EmpresaConfig(),
                    isLoading = false
                )
            }
        }
    }

    fun updateField(field: String, value: String) {
        val current = _uiState.value.config
        _uiState.value = _uiState.value.copy(
            config = when (field) {
                "nomeEmpresa" -> current.copy(nomeEmpresa = value)
                "cnpj" -> current.copy(cnpj = value)
                "telefone" -> current.copy(telefone = value)
                "whatsapp" -> current.copy(whatsapp = value)
                "email" -> current.copy(email = value)
                "endereco" -> current.copy(endereco = value)
                "cidade" -> current.copy(cidade = value)
                "estado" -> current.copy(estado = value)
                "cep" -> current.copy(cep = value)
                "logoPath" -> current.copy(logoPath = value)
                else -> current
            }
        )
    }

    fun saveConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                repository.saveConfig(_uiState.value.config)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Erro ao salvar: ${e.message}"
                )
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    class Factory(private val repository: EmpresaConfigRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConfigViewModel(repository) as T
        }
    }
}
