package com.tecmanutencao.app.ui.visita

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecmanutencao.app.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitaFormScreen(
    visitaId: Long,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: VisitaViewModel = viewModel(factory = VisitaViewModel.Factory(
        (LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).visitaRepository
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEditing = visitaId > 0

    LaunchedEffect(visitaId) {
        if (visitaId > 0) {
            viewModel.loadVisita(visitaId)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.resetSaveSuccess()
            onSave()
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Excluir Visita",
            message = "Tem certeza que deseja excluir esta visita?",
            confirmText = "Excluir",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteVisita()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Visita" else "Nova Visita") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.currentVisita.nomeCliente,
                onValueChange = { viewModel.updateField("nomeCliente", it) },
                label = { Text("Nome do Cliente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentVisita.endereco,
                onValueChange = { viewModel.updateField("endereco", it) },
                label = { Text("Endereço da Visita") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = if (uiState.currentVisita.valor > 0) uiState.currentVisita.valor.toString() else "",
                onValueChange = {
                    val value = it.replace(",", ".").toDoubleOrNull() ?: 0.0
                    viewModel.updateField("valor", value)
                },
                label = { Text("Valor (R$)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentVisita.problemaRelatado,
                onValueChange = { viewModel.updateField("problemaRelatado", it) },
                label = { Text("Problema Relatado") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = uiState.currentVisita.solucao,
                onValueChange = { viewModel.updateField("solucao", it) },
                label = { Text("Solução Aplicada") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = uiState.currentVisita.observacoes,
                onValueChange = { viewModel.updateField("observacoes", it) },
                label = { Text("Observações") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 2
            )

            Button(
                onClick = { viewModel.saveVisita() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isSaving
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Salvar Visita", modifier = Modifier.padding(start = 4.dp))
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("  Excluir", modifier = Modifier.padding(start = 4.dp))
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
