package com.tecmanutencao.app.ui.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.domain.model.Cliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

data class ClienteUiState(
    val clientes: List<Cliente> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val currentCliente: Cliente = Cliente(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ClienteViewModel(
    private val repository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState: StateFlow<ClienteUiState> = _uiState.asStateFlow()

    init {
        loadClientes()
        setupSearch()
    }

    private fun loadClientes() {
        viewModelScope.launch {
            repository.getAllClientes().collect { clientes ->
                _uiState.value = _uiState.value.copy(
                    clientes = clientes,
                    isLoading = false
                )
            }
        }
    }

    private fun setupSearch() {
        viewModelScope.launch {
            _uiState
                .debounce(300)
                .distinctUntilChanged()
                .collect { state ->
                    val query = state.searchQuery
                    if (query.isNotEmpty()) {
                        repository.searchClientes(query).collect { clientes ->
                            _uiState.value = _uiState.value.copy(clientes = clientes)
                        }
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isEmpty()) {
            loadClientes()
        }
    }

    fun loadCliente(id: Long) {
        viewModelScope.launch {
            val cliente = repository.getClienteById(id)
            if (cliente != null) {
                _uiState.value = _uiState.value.copy(currentCliente = cliente)
            }
        }
    }

    fun updateField(field: String, value: String) {
        val current = _uiState.value.currentCliente
        _uiState.value = _uiState.value.copy(
            currentCliente = when (field) {
                "nomeCompleto" -> current.copy(nomeCompleto = value)
                "cpfCnpj" -> current.copy(cpfCnpj = value)
                "telefone" -> current.copy(telefone = value)
                "whatsapp" -> current.copy(whatsapp = value)
                "email" -> current.copy(email = value)
                "endereco" -> current.copy(endereco = value)
                "cidade" -> current.copy(cidade = value)
                "estado" -> current.copy(estado = value)
                "cep" -> current.copy(cep = value)
                "observacoes" -> current.copy(observacoes = value)
                else -> current
            }
        )
    }

    fun saveCliente() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val cliente = _uiState.value.currentCliente
                if (cliente.id == 0L) {
                    repository.saveCliente(cliente)
                } else {
                    repository.updateCliente(cliente)
                }
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Erro ao salvar: ${e.message}"
                )
            }
        }
    }

    fun deleteCliente() {
        viewModelScope.launch {
            try {
                repository.deleteCliente(_uiState.value.currentCliente)
                _uiState.value = _uiState.value.copy(saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Erro ao excluir: ${e.message}"
                )
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }

    class Factory(private val repository: ClienteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClienteViewModel(repository) as T
        }
    }
}
