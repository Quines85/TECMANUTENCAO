package com.tecmanutencao.app.ui.config

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onBack: () -> Unit,
    viewModel: ConfigViewModel = viewModel(factory = ConfigViewModel.Factory(
        (LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).empresaConfigRepository
    ))
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
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
            Text(
                "Dados da Empresa",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = uiState.config.nomeEmpresa,
                onValueChange = { viewModel.updateField("nomeEmpresa", it) },
                label = { Text("Nome da Empresa") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.cnpj,
                onValueChange = { viewModel.updateField("cnpj", it) },
                label = { Text("CNPJ") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.telefone,
                onValueChange = { viewModel.updateField("telefone", it) },
                label = { Text("Telefone") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.whatsapp,
                onValueChange = { viewModel.updateField("whatsapp", it) },
                label = { Text("WhatsApp") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.email,
                onValueChange = { viewModel.updateField("email", it) },
                label = { Text("E-mail") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.endereco,
                onValueChange = { viewModel.updateField("endereco", it) },
                label = { Text("Endereço") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.cidade,
                onValueChange = { viewModel.updateField("cidade", it) },
                label = { Text("Cidade") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.estado,
                onValueChange = { viewModel.updateField("estado", it) },
                label = { Text("Estado") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.config.cep,
                onValueChange = { viewModel.updateField("cep", it) },
                label = { Text("CEP") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            Button(
                onClick = { viewModel.saveConfig() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isSaving
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Salvar Configurações", modifier = Modifier.padding(start = 4.dp))
            }

            if (uiState.saveSuccess) {
                Text(
                    text = "Configurações salvas com sucesso!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
