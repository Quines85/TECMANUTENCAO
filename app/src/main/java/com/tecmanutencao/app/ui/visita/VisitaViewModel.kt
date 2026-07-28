package com.tecmanutencao.app.ui.visita

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.data.repository.VisitaRepository
import com.tecmanutencao.app.domain.model.StatusVisita
import com.tecmanutencao.app.domain.model.Visita
import com.tecmanutencao.app.util.NumberUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VisitaUiState(
    val visitas: List<Visita> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val currentVisita: Visita = Visita(),
    val totalGeral: Double = 0.0,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class VisitaViewModel(
    private val repository: VisitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisitaUiState())
    val uiState: StateFlow<VisitaUiState> = _uiState.asStateFlow()

    init {
        loadVisitas()
    }

    private fun loadVisitas() {
        viewModelScope.launch {
            repository.getAllVisitas().collect { visitas ->
                val total = repository.getTotalVisitas()
                _uiState.value = _uiState.value.copy(
                    visitas = visitas,
                    totalGeral = total,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadVisitas()
            } else {
                repository.searchVisitas(query).collect { visitas ->
                    _uiState.value = _uiState.value.copy(visitas = visitas)
                }
            }
        }
    }

    fun loadVisita(id: Long) {
        viewModelScope.launch {
            val visita = repository.getVisitaById(id)
            if (visita != null) {
                _uiState.value = _uiState.value.copy(currentVisita = visita)
            }
        }
    }

    fun updateField(field: String, value: Any) {
        val current = _uiState.value.currentVisita
        _uiState.value = _uiState.value.copy(
            currentVisita = when (field) {
                "nomeCliente" -> current.copy(nomeCliente = value as String)
                "endereco" -> current.copy(endereco = value as String)
                "problemaRelatado" -> current.copy(problemaRelatado = value as String)
                "solucao" -> current.copy(solucao = value as String)
                "valor" -> current.copy(valor = value as Double)
                "observacoes" -> current.copy(observacoes = value as String)
                "status" -> current.copy(status = value as StatusVisita)
                else -> current
            }
        )
    }

    fun saveVisita() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val visita = _uiState.value.currentVisita
                if (visita.id == 0L) {
                    repository.saveVisita(visita)
                } else {
                    repository.updateVisita(visita)
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

    fun deleteVisita() {
        viewModelScope.launch {
            try {
                repository.deleteVisita(_uiState.value.currentVisita)
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

    class Factory(private val repository: VisitaRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VisitaViewModel(repository) as T
        }
    }
}
