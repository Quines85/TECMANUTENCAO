package com.tecmanutencao.app.ui.orcamento

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecmanutencao.app.domain.model.FormaPagamento
import com.tecmanutencao.app.domain.model.TipoMaquina
import com.tecmanutencao.app.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcamentoFormScreen(
    orcamentoId: Long,
    onSave: () -> Unit,
    onBack: () -> Unit,
    viewModel: OrcamentoViewModel = viewModel(factory = OrcamentoViewModel.Factory(
        (LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).orcamentoRepository,
        (LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).clienteRepository,
        (LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).equipamentoRepository
    ))
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = orcamentoId > 0

    var tipoMaquinaExpanded by remember { mutableStateOf(false) }
    var formaPagamentoExpanded by remember { mutableStateOf(false) }
    var clienteExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(orcamentoId) {
        if (orcamentoId > 0) {
            viewModel.loadOrcamento(orcamentoId)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.resetSaveSuccess()
            onSave()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Orçamento" else "Novo Orçamento") },
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
            // Select Cliente
            ExposedDropdownMenuBox(
                expanded = clienteExpanded,
                onExpandedChange = { clienteExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.currentCliente.nomeCompleto.ifEmpty { "Selecione um cliente" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clienteExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = clienteExpanded,
                    onDismissRequest = { clienteExpanded = false }
                ) {
                    uiState.clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = { Text("${cliente.nomeCompleto} - ${cliente.telefone}") },
                            onClick = {
                                viewModel.selectCliente(cliente.id)
                                clienteExpanded = false
                            }
                        )
                    }
                }
            }

            // Tipo de Máquina
            ExposedDropdownMenuBox(
                expanded = tipoMaquinaExpanded,
                onExpandedChange = { tipoMaquinaExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.currentEquipamento.tipoMaquina.descricao,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo da Máquina") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoMaquinaExpanded) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = tipoMaquinaExpanded,
                    onDismissRequest = { tipoMaquinaExpanded = false }
                ) {
                    TipoMaquina.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.descricao) },
                            onClick = {
                                viewModel.updateEquipamentoField("tipoMaquina", tipo)
                                tipoMaquinaExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.currentEquipamento.marca,
                onValueChange = { viewModel.updateEquipamentoField("marca", it) },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentEquipamento.modelo,
                onValueChange = { viewModel.updateEquipamentoField("modelo", it) },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentEquipamento.numeroSerie,
                onValueChange = { viewModel.updateEquipamentoField("numeroSerie", it) },
                label = { Text("Número de Série") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.currentEquipamento.problemaInformado,
                onValueChange = { viewModel.updateEquipamentoField("problemaInformado", it) },
                label = { Text("Problema Informado") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = uiState.currentEquipamento.observacoesTecnico,
                onValueChange = { viewModel.updateEquipamentoField("observacoesTecnico", it) },
                label = { Text("Observações do Técnico") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = uiState.currentOrcamento.descricaoServico,
                onValueChange = { viewModel.updateOrcamentoField("descricaoServico", it) },
                label = { Text("Descrição do Serviço") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 4
            )

            OutlinedTextField(
                value = if (uiState.currentOrcamento.valorServico > 0)
                    uiState.currentOrcamento.valorServico.toString() else "",
                onValueChange = {
                    val value = it.replace(",", ".").toDoubleOrNull() ?: 0.0
                    viewModel.updateOrcamentoField("valorServico", value)
                },
                label = { Text("Valor do Serviço (R$)") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Forma de Pagamento
            ExposedDropdownMenuBox(
                expanded = formaPagamentoExpanded,
                onExpandedChange = { formaPagamentoExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.currentOrcamento.formaPagamento.descricao,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Forma de Pagamento") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formaPagamentoExpanded) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = formaPagamentoExpanded,
                    onDismissRequest = { formaPagamentoExpanded = false }
                ) {
                    FormaPagamento.entries.forEach { forma ->
                        DropdownMenuItem(
                            text = { Text(forma.descricao) },
                            onClick = {
                                viewModel.updateOrcamentoField("formaPagamento", forma)
                                formaPagamentoExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = uiState.currentOrcamento.observacoes,
                onValueChange = { viewModel.updateOrcamentoField("observacoes", it) },
                label = { Text("Observações") },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                minLines = 3
            )

            Button(
                onClick = { viewModel.saveOrcamento() },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                enabled = !uiState.isSaving && uiState.selectedClienteId > 0
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text("  Salvar Orçamento", modifier = Modifier.padding(start = 4.dp))
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
