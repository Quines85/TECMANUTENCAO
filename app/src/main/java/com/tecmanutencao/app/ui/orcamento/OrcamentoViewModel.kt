package com.tecmanutencao.app.ui.orcamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.EquipamentoRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository
import com.tecmanutencao.app.domain.model.Cliente
import com.tecmanutencao.app.domain.model.Equipamento
import com.tecmanutencao.app.domain.model.FormaPagamento
import com.tecmanutencao.app.domain.model.Orcamento
import com.tecmanutencao.app.domain.model.StatusOrcamento
import com.tecmanutencao.app.domain.model.TipoMaquina
import com.tecmanutencao.app.util.NumberUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrcamentoUiState(
    val orcamentos: List<Orcamento> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val isLoading: Boolean = true,
    val currentOrcamento: Orcamento = Orcamento(),
    val currentEquipamento: Equipamento = Equipamento(),
    val currentCliente: Cliente = Cliente(),
    val selectedClienteId: Long = 0,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class OrcamentoViewModel(
    private val orcamentoRepository: OrcamentoRepository,
    private val clienteRepository: ClienteRepository,
    private val equipamentoRepository: EquipamentoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrcamentoUiState())
    val uiState: StateFlow<OrcamentoUiState> = _uiState.asStateFlow()

    init {
        loadOrcamentos()
        loadClientes()
    }

    private fun loadOrcamentos() {
        viewModelScope.launch {
            orcamentoRepository.getAllOrcamentos().collect { orcamentos ->
                _uiState.value = _uiState.value.copy(
                    orcamentos = orcamentos,
                    isLoading = false
                )
            }
        }
    }

    private fun loadClientes() {
        viewModelScope.launch {
            clienteRepository.getAllClientes().collect { clientes ->
                _uiState.value = _uiState.value.copy(clientes = clientes)
            }
        }
    }

    fun loadOrcamento(id: Long) {
        viewModelScope.launch {
            val orcamento = orcamentoRepository.getOrcamentoById(id)
            if (orcamento != null) {
                _uiState.value = _uiState.value.copy(currentOrcamento = orcamento)
                if (orcamento.clienteId > 0) {
                    val cliente = clienteRepository.getClienteById(orcamento.clienteId)
                    _uiState.value = _uiState.value.copy(
                        currentCliente = cliente ?: Cliente(),
                        selectedClienteId = orcamento.clienteId
                    )
                }
                if (orcamento.equipamentoId > 0) {
                    val equip = equipamentoRepository.getEquipamentoById(orcamento.equipamentoId)
                    if (equip != null) {
                        _uiState.value = _uiState.value.copy(currentEquipamento = equip)
                    }
                }
            }
        }
    }

    fun selectCliente(clienteId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedClienteId = clienteId)
            if (clienteId > 0) {
                val cliente = clienteRepository.getClienteById(clienteId)
                if (cliente != null) {
                    _uiState.value = _uiState.value.copy(currentCliente = cliente)
                }
            }
        }
    }

    fun updateOrcamentoField(field: String, value: Any) {
        val current = _uiState.value.currentOrcamento
        _uiState.value = _uiState.value.copy(
            currentOrcamento = when (field) {
                "descricaoServico" -> current.copy(descricaoServico = value as String)
                "valorServico" -> current.copy(valorServico = (value as Double))
                "formaPagamento" -> current.copy(formaPagamento = value as FormaPagamento)
                "observacoes" -> current.copy(observacoes = value as String)
                else -> current
            }
        )
    }

    fun updateEquipamentoField(field: String, value: Any) {
        val current = _uiState.value.currentEquipamento
        _uiState.value = _uiState.value.copy(
            currentEquipamento = when (field) {
                "tipoMaquina" -> current.copy(tipoMaquina = value as TipoMaquina)
                "marca" -> current.copy(marca = value as String)
                "modelo" -> current.copy(modelo = value as String)
                "numeroSerie" -> current.copy(numeroSerie = value as String)
                "problemaInformado" -> current.copy(problemaInformado = value as String)
                "observacoesTecnico" -> current.copy(observacoesTecnico = value as String)
                else -> current
            }
        )
    }

    fun updateStatus(status: StatusOrcamento) {
        val current = _uiState.value.currentOrcamento
        _uiState.value = _uiState.value.copy(
            currentOrcamento = current.copy(status = status)
        )
        saveOrcamento()
    }

    fun saveOrcamento() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val state = _uiState.value
                val orcamento = state.currentOrcamento.copy(
                    clienteId = state.selectedClienteId
                )

                val orcamentoId: Long
                if (orcamento.id == 0L) {
                    val count = orcamentoRepository.getOrcamentoCount() + 1
                    val finalOrcamento = orcamento.copy(
                        numeroOrcamento = NumberUtils.formatOrcamentoNumber(count)
                    )
                    orcamentoId = orcamentoRepository.saveOrcamento(finalOrcamento)

                    val equip = state.currentEquipamento.copy(orcamentoId = orcamentoId)
                    equipamentoRepository.saveEquipamento(equip)

                    _uiState.value = _uiState.value.copy(
                        currentOrcamento = finalOrcamento.copy(id = orcamentoId)
                    )
                } else {
                    orcamentoRepository.updateOrcamento(orcamento)
                    val equip = state.currentEquipamento.copy(orcamentoId = orcamento.id)
                    equipamentoRepository.updateEquipamento(equip)
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

    fun deleteOrcamento() {
        viewModelScope.launch {
            try {
                val orcamento = _uiState.value.currentOrcamento
                if (orcamento.equipamentoId > 0) {
                    equipamentoRepository.deleteByOrcamentoId(orcamento.id)
                }
                orcamentoRepository.deleteOrcamento(orcamento)
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

    class Factory(
        private val orcamentoRepository: OrcamentoRepository,
        private val clienteRepository: ClienteRepository,
        private val equipamentoRepository: EquipamentoRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrcamentoViewModel(orcamentoRepository, clienteRepository, equipamentoRepository) as T
        }
    }
}
