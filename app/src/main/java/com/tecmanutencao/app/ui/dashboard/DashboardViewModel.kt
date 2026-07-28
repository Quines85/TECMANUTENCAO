package com.tecmanutencao.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository
import com.tecmanutencao.app.data.repository.VisitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class MonthlyDashboardData(
    val monthLabel: String,
    val orcamentosTotal: Int,
    val orcamentosFinalizados: Int,
    val visitasFinalizadas: Int,
    val novosClientes: Int,
    val lucroTotal: Double
)

data class DashboardUiState(
    val monthlyData: List<MonthlyDashboardData> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
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
