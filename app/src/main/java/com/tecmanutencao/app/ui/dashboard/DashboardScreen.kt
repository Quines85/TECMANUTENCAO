package com.tecmanutencao.app.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecmanutencao.app.TecManutencaoApp
import com.tecmanutencao.app.ui.components.EmptyState
import com.tecmanutencao.app.util.DateUtils

private data class BarColors(
    val orcamentos: Color = Color(0xFF2196F3),
    val finalized: Color = Color(0xFF4CAF50),
    val visitas: Color = Color(0xFFFF9800),
    val clientes: Color = Color(0xFF9C27B0),
    val lucro: Color = Color(0xFFF44336)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onBack: () -> Unit) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as TecManutencaoApp
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(
            app.orcamentoRepository,
            app.visitaRepository,
            app.clienteRepository
        )
    )
    val state by viewModel.uiState.collectAsState()

    if (state.detailState.isVisible) {
        DetailDialog(state = state.detailState, onDismiss = { viewModel.hideDetail() })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                LinearProgressIndicator()
            }
        } else if (state.errorMessage != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.errorMessage ?: "", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
        } else if (state.monthlyData.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("Nenhum dado encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                MonthSelector(
                    months = state.monthlyData.map { it.monthLabel },
                    selectedIndex = state.selectedMonthIndex,
                    onSelect = { viewModel.selectMonth(it) }
                )
                Spacer(Modifier.height(16.dp))
                val month = state.monthlyData.getOrElse(state.selectedMonthIndex) { return@Column }
                BarChart(data = month)
                Spacer(Modifier.height(16.dp))
                StatsRow(
                    data = month,
                    onOrcamentosClick = { viewModel.showDetail(DetailType.ORCAMENTOS) },
                    onFinalizadosClick = { viewModel.showDetail(DetailType.FINALIZADOS) },
                    onVisitasClick = { viewModel.showDetail(DetailType.VISITAS) },
                    onClientesClick = { viewModel.showDetail(DetailType.CLIENTES) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthSelector(months: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedMonth = months.getOrElse(selectedIndex) { "" }
    Box {
        OutlinedTextField(
            value = selectedMonth,
            onValueChange = {},
            readOnly = true,
            label = { Text("Selecionar mês") },
            trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = "Abrir") },
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEachIndexed { index, label ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(index); expanded = false })
            }
        }
    }
}

@Composable
private fun BarChart(data: MonthlyDashboardData) {
    val colors = BarColors()
    val bars = listOf(
        "Orçamentos" to data.orcamentosTotal.toFloat() to colors.orcamentos,
        "Finalizados" to data.orcamentosFinalizados.toFloat() to colors.finalized,
        "Visitas" to data.visitasFinalizadas.toFloat() to colors.visitas,
        "Clientes" to data.novosClientes.toFloat() to colors.clientes,
        "Lucro (R\$)" to data.lucroTotal.toFloat() to colors.lucro
    )
    val maxVal = bars.maxOf { (it.first).second }.coerceAtLeast(1f)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Visão Geral - ${data.monthLabel}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                val w = size.width; val h = size.height
                val l = 100f; val r = 20f; val t = 10f; val b = 20f
                val cw = w - l - r; val ch = (h - t - b) / bars.size
                val barH = ch * 0.6f
                bars.forEachIndexed { idx, bar ->
                    val (label, value) = bar.first
                    val color = bar.second
                    val barW = cw * (value / maxVal)
                    val y = t + idx * ch + (ch - barH) / 2

                    drawLine(Color(0xFFE0E0E0), Offset(l, y + barH / 2), Offset(w - r, y + barH / 2), strokeWidth = 1f)
                    drawRect(color, Offset(l, y), Size(barW.coerceAtLeast(4f), barH))
                    drawRect(color.copy(alpha = 0.3f), Offset(l + barW.coerceAtLeast(4f), y), Size(cw - barW.coerceAtLeast(4f), barH))
                }
            }
            Spacer(Modifier.height(8.dp))
            bars.forEachIndexed { idx, bar ->
                val label = bar.first.first
                val value = bar.first.second
                val color = bar.second
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(Modifier.size(10.dp)) { drawCircle(color) }
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = if (idx == 4) "R$ ${"%.2f".format(value)}" else value.toInt().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(data: MonthlyDashboardData, onOrcamentosClick: () -> Unit, onFinalizadosClick: () -> Unit, onVisitasClick: () -> Unit, onClientesClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(data.monthLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ClickableStatItem("Orçamentos", data.orcamentosTotal.toString(), onClick = onOrcamentosClick)
                ClickableStatItem("Finalizados", data.orcamentosFinalizados.toString(), onClick = onFinalizadosClick)
                ClickableStatItem("Visitas", data.visitasFinalizadas.toString(), onClick = onVisitasClick)
                ClickableStatItem("Clientes", data.novosClientes.toString(), onClick = onClientesClick)
            }
            Spacer(Modifier.height(12.dp))
            Text("Lucro: R$ ${"%.2f".format(data.lucroTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ClickableStatItem(label: String, value: String, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.width(80.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailDialog(state: DetailState, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (state.type) {
                    DetailType.ORCAMENTOS, DetailType.FINALIZADOS -> {
                        if (state.orcamentos.isEmpty()) EmptyState("Nenhum orçamento encontrado")
                        else state.orcamentos.forEach { orc ->
                            DetailRow(
                                "Orçamento #${orc.numeroOrcamento}",
                                "R$ ${"%.2f".format(orc.valorServico)} - ${orc.status.descricao}",
                                DateUtils.formatDate(orc.data)
                            )
                        }
                    }
                    DetailType.VISITAS -> {
                        if (state.visitas.isEmpty()) EmptyState("Nenhuma visita encontrada")
                        else state.visitas.forEach { v ->
                            DetailRow(
                                v.nomeCliente,
                                "${v.endereco} [${v.status.descricao}]",
                                "R$ ${"%.2f".format(v.valor)} - ${DateUtils.formatDate(v.data)}"
                            )
                        }
                    }
                    DetailType.CLIENTES -> {
                        if (state.clientes.isEmpty()) EmptyState("Nenhum cliente encontrado")
                        else state.clientes.forEach { c ->
                            DetailRow(c.nomeCompleto, c.telefone, c.email)
                        }
                    }
                    null -> {}
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}

@Composable
private fun DetailRow(primary: String, secondary: String, tertiary: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(secondary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(tertiary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
