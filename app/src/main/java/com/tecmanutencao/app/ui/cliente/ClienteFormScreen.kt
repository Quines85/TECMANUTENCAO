package com.tecmanutencao.app.ui.cliente

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecmanutencao.app.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteFormScreen(
    clienteId: Long,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: ClienteViewModel = viewModel(factory = ClienteViewModel.Factory(
        (androidx.compose.ui.platform.LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).clienteRepository
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isEditing = clienteId > 0

    LaunchedEffect(clienteId) {
        if (clienteId > 0) {
            viewModel.loadCliente(clienteId)
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
            title = "Excluir Cliente",
            message = "Tem certeza que deseja excluir este cliente? Esta ação não pode ser desfeita.",
            confirmText = "Excluir",
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteCliente()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Cliente" else "Novo Cliente") },
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
                value = uiState.currentCliente.nomeCompleto,
                onValueChange = { viewModel.updateField("nomeCompleto", it) },
                label = { Text("Nome Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.cpfCnpj,
                onValueChange = { viewModel.updateField("cpfCnpj", it) },
                label = { Text("CPF/CNPJ") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.telefone,
                onValueChange = { viewModel.updateField("telefone", it) },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.whatsapp,
                onValueChange = { viewModel.updateField("whatsapp", it) },
                label = { Text("WhatsApp") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.email,
                onValueChange = { viewModel.updateField("email", it) },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.endereco,
                onValueChange = { viewModel.updateField("endereco", it) },
                label = { Text("Endereço") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.cidade,
                onValueChange = { viewModel.updateField("cidade", it) },
                label = { Text("Cidade") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.estado,
                onValueChange = { viewModel.updateField("estado", it) },
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.cep,
                onValueChange = { viewModel.updateField("cep", it) },
                label = { Text("CEP") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentCliente.observacoes,
                onValueChange = { viewModel.updateField("observacoes", it) },
                label = { Text("Observações") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3
            )

            Button(
                onClick = { viewModel.saveCliente() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isSaving
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Salvar", modifier = Modifier.padding(start = 4.dp))
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
