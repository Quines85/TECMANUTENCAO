package com.tecmanutencao.app.ui.orcamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecmanutencao.app.domain.model.Orcamento
import com.tecmanutencao.app.domain.model.StatusOrcamento
import com.tecmanutencao.app.ui.components.EmptyState
import com.tecmanutencao.app.util.DateUtils
import com.tecmanutencao.app.util.NumberUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcamentoListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: OrcamentoViewModel = viewModel(factory = OrcamentoViewModel.Factory(
        (androidx.compose.ui.platform.LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).orcamentoRepository,
        (androidx.compose.ui.platform.LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).clienteRepository,
        (androidx.compose.ui.platform.LocalContext.current.applicationContext as com.tecmanutencao.app.TecManutencaoApp).equipamentoRepository
    ))
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orçamentos") },
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
        if (uiState.isLoading) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (uiState.orcamentos.isEmpty()) {
            EmptyState(
                message = "Nenhum orçamento encontrado",
                icon = Icons.Default.Description
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(uiState.orcamentos, key = { it.id }) { orcamento ->
                    OrcamentoCard(
                        orcamento = orcamento,
                        onClick = { onNavigateToDetail(orcamento.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrcamentoCard(orcamento: Orcamento, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Build,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = orcamento.numeroOrcamento,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Data: ${DateUtils.formatDate(orcamento.data)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Valor: ${NumberUtils.formatCurrency(orcamento.valorServico)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            StatusBadge(orcamento.status)
        }
    }
}

@Composable
private fun StatusBadge(status: StatusOrcamento) {
    val color = when (status) {
        StatusOrcamento.ABERTO -> MaterialTheme.colorScheme.primary
        StatusOrcamento.AGUARDANDO_APROVACAO -> MaterialTheme.colorScheme.tertiary
        StatusOrcamento.APROVADO -> MaterialTheme.colorScheme.secondary
        StatusOrcamento.RECUSADO -> MaterialTheme.colorScheme.error
        StatusOrcamento.FINALIZADO -> MaterialTheme.colorScheme.outline
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = status.descricao,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
