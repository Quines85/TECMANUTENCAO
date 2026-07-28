package com.tecmanutencao.app.ui.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tecmanutencao.app.TecManutencaoApp
import com.tecmanutencao.app.data.backup.BackupService
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.EmpresaConfigRepository
import com.tecmanutencao.app.data.repository.EquipamentoRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository
import com.tecmanutencao.app.data.repository.VisitaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportedFile: File? = null,
    val exportPath: String? = null,
    val importResult: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class BackupViewModel(
    application: Application,
    private val clienteRepository: ClienteRepository,
    private val orcamentoRepository: OrcamentoRepository,
    private val equipamentoRepository: EquipamentoRepository,
    private val visitaRepository: VisitaRepository,
    private val empresaConfigRepository: EmpresaConfigRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, errorMessage = null, successMessage = null)
            try {
                val result = BackupService.exportBackup(
                    context = getApplication(),
                    clienteRepo = clienteRepository,
                    orcamentoRepo = orcamentoRepository,
                    equipamentoRepo = equipamentoRepository,
                    visitaRepo = visitaRepository,
                    empresaConfigRepo = empresaConfigRepository
                )
                val pathMsg = if (result.publicPath != null) {
                    "Salvo em: ${result.publicPath}"
                } else {
                    "Arquivo interno: ${result.file.absolutePath}"
                }
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportedFile = result.file,
                    exportPath = result.publicPath ?: result.file.absolutePath,
                    successMessage = "Backup exportado com sucesso!\n$pathMsg"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "Erro ao exportar: ${e.message}"
                )
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, errorMessage = null, successMessage = null, importResult = null)
            try {
                val result = BackupService.importBackup(
                    context = getApplication(),
                    uri = uri,
                    clienteRepo = clienteRepository,
                    orcamentoRepo = orcamentoRepository,
                    equipamentoRepo = equipamentoRepository,
                    visitaRepo = visitaRepository,
                    empresaConfigRepo = empresaConfigRepository
                )
                when (result) {
                    is BackupService.ImportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            successMessage = "${result.count} registros importados com sucesso!"
                        )
                    }
                    is BackupService.ImportResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            errorMessage = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = "Erro ao importar: ${e.message}"
                )
            }
        }
    }

    fun resetMessages() {
        _uiState.value = _uiState.value.copy(
            exportedFile = null,
            exportPath = null,
            importResult = null,
            errorMessage = null,
            successMessage = null
        )
    }

    class Factory(
        private val application: Application
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val app = application as TecManutencaoApp
            return BackupViewModel(
                application = application,
                clienteRepository = app.clienteRepository,
                orcamentoRepository = app.orcamentoRepository,
                equipamentoRepository = app.equipamentoRepository,
                visitaRepository = app.visitaRepository,
                empresaConfigRepository = app.empresaConfigRepository
            ) as T
        }
    }
}
