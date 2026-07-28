package com.tecmanutencao.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository
import com.tecmanutencao.app.data.repository.VisitaRepository
import com.tecmanutencao.app.domain.model.Cliente
import com.tecmanutencao.app.domain.model.Orcamento
import com.tecmanutencao.app.domain.model.Visita
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlyDashboardData(
    val monthLabel: String,
    val monthShort: String,
    val orcamentosTotal: Int,
    val orcamentosFinalizados: Int,
    val visitasFinalizadas: Int,
    val novosClientes: Int,
    val lucroTotal: Double
)

enum class DetailType { ORCAMENTOS, FINALIZADOS, VISITAS, CLIENTES }

data class DetailState(
    val type: DetailType? = null,
    val title: String = "",
    val orcamentos: List<Orcamento> = emptyList(),
    val visitas: List<Visita> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val isVisible: Boolean = false
)

data class DashboardUiState(
    val monthlyData: List<MonthlyDashboardData> = emptyList(),
    val selectedMonthIndex: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val detailState: DetailState = DetailState()
)

class DashboardViewModel(
    private val orcamentoRepository: OrcamentoRepository,
    private val visitaRepository: VisitaRepository,
    private val clienteRepository: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val data = mutableListOf<MonthlyDashboardData>()
                val cal = Calendar.getInstance()

                for (i in 0 until 12) {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.add(Calendar.MONTH, -i)

                    val monthStart = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val monthEnd = cal.timeInMillis

                    val monthNames = arrayOf(
                        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
                    )
                    val monthShort = arrayOf("Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez")
                    val monthLabel = "${monthNames[cal.get(Calendar.MONTH)]}/${cal.get(Calendar.YEAR)}"

                    val orcTotal = orcamentoRepository.getOrcamentoCountBetween(monthStart, monthEnd)
                    val orcFinalizados = orcamentoRepository.getFinalizedOrcamentoCountBetween(monthStart, monthEnd)
                    val visitas = visitaRepository.getVisitaCountBetween(monthStart, monthEnd)
                    val novosClientes = clienteRepository.getNewClientCountBetween(monthStart, monthEnd)
                    val lucroOrcamentos = orcamentoRepository.getOrcamentoProfitBetween(monthStart, monthEnd)
                    val lucroVisitas = visitaRepository.getVisitaProfitBetween(monthStart, monthEnd)

                    data.add(
                        MonthlyDashboardData(
                            monthLabel = monthLabel,
                            monthShort = monthShort[cal.get(Calendar.MONTH)],
                            orcamentosTotal = orcTotal,
                            orcamentosFinalizados = orcFinalizados,
                            visitasFinalizadas = visitas,
                            novosClientes = novosClientes,
                            lucroTotal = lucroOrcamentos + lucroVisitas
                        )
                    )
                }

                _uiState.value = _uiState.value.copy(monthlyData = data, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Erro ao carregar: ${e.message}"
                )
            }
        }
    }

    fun selectMonth(index: Int) {
        _uiState.value = _uiState.value.copy(selectedMonthIndex = index)
    }

    fun showDetail(type: DetailType) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedMonthIndex >= state.monthlyData.size) return@launch
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, -state.selectedMonthIndex)
            val monthStart = cal.timeInMillis
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val monthEnd = cal.timeInMillis

            val monthLabel = state.monthlyData[state.selectedMonthIndex].monthLabel
            val title = when (type) {
                DetailType.ORCAMENTOS -> "Orçamentos - $monthLabel"
                DetailType.FINALIZADOS -> "Finalizados - $monthLabel"
                DetailType.VISITAS -> "Visitas - $monthLabel"
                DetailType.CLIENTES -> "Clientes - $monthLabel"
            }

            val orcamentos = if (type == DetailType.ORCAMENTOS || type == DetailType.FINALIZADOS) {
                if (type == DetailType.FINALIZADOS) {
                    orcamentoRepository.getFinalizedOrcamentosBetween(monthStart, monthEnd)
                } else {
                    orcamentoRepository.getOrcamentosBetween(monthStart, monthEnd)
                }
            } else emptyList()

            val visitas = if (type == DetailType.VISITAS) {
                visitaRepository.getVisitasBetween(monthStart, monthEnd)
            } else emptyList()

            val clientes = if (type == DetailType.CLIENTES) {
                clienteRepository.getClientesBetween(monthStart, monthEnd)
            } else emptyList()

            _uiState.value = _uiState.value.copy(
                detailState = DetailState(
                    type = type,
                    title = title,
                    orcamentos = orcamentos,
                    visitas = visitas,
                    clientes = clientes,
                    isVisible = true
                )
            )
        }
    }

    fun hideDetail() {
        _uiState.value = _uiState.value.copy(detailState = DetailState())
    }

    class Factory(
        private val orcamentoRepository: OrcamentoRepository,
        private val visitaRepository: VisitaRepository,
        private val clienteRepository: ClienteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(orcamentoRepository, visitaRepository, clienteRepository) as T
        }
    }
}
