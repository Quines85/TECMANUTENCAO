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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tecmanutencao.app.TecManutencaoApp
import com.tecmanutencao.app.ui.components.EmptyState
import com.tecmanutencao.app.util.DateUtils

private data class LineChartColors(
    val orcamentos: Color = Color(0xFF2196F3),
    val finalized: Color = Color(0xFF4CAF50),
    val visitas: Color = Color(0xFFFF9800),
    val clientes: Color = Color(0xFF9C27B0),
    val lucro: Color = Color(0xFFF44336),
    val grid: Color = Color(0xFFE0E0E0),
    val axis: Color = Color(0xFF616161)
)

private data class ChartSerie(
    val values: List<Float>,
    val max: Float,
    val color: Color
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
                LineChart(data = state.monthlyData)
                Spacer(Modifier.height(16.dp))
                val month = state.monthlyData.getOrElse(state.selectedMonthIndex) { return@Column }
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
private fun LineChart(data: List<MonthlyDashboardData>) {
    val colors = LineChartColors()
    val series = listOf(
        ChartSerie(data.map { it.orcamentosTotal.toFloat() }, data.maxOfOrNull { it.orcamentosTotal }?.toFloat() ?: 1f, colors.orcamentos),
        ChartSerie(data.map { it.orcamentosFinalizados.toFloat() }, data.maxOfOrNull { it.orcamentosFinalizados }?.toFloat() ?: 1f, colors.finalized),
        ChartSerie(data.map { it.visitasFinalizadas.toFloat() }, data.maxOfOrNull { it.visitasFinalizadas }?.toFloat() ?: 1f, colors.visitas),
        ChartSerie(data.map { it.novosClientes.toFloat() }, data.maxOfOrNull { it.novosClientes }?.toFloat() ?: 1f, colors.clientes),
        ChartSerie(data.map { it.lucroTotal.toFloat() }, data.maxOfOrNull { it.lucroTotal }?.toFloat() ?: 1f, colors.lucro)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Tendência (12 meses)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val w = size.width; val h = size.height
                val l = 40f; val b = 40f; val t = 10f; val r = 10f
                val cw = w - l - r; val ch = h - t - b

                fun x(idx: Int) = l + cw * idx / (data.size - 1).coerceAtLeast(1)
                fun y(v: Float, mx: Float) = t + ch * (1f - v / mx)

                for (i in 0..4) drawLine(colors.grid, Offset(l, t + ch * i / 4), Offset(w - r, t + ch * i / 4), strokeWidth = 1f)
                data.indices.forEach { i -> drawLine(colors.grid, Offset(x(i), t), Offset(x(i), t + ch), strokeWidth = 1f) }

                series.filter { it.max > 0 }.forEach { serie ->
                    val path = Path()
                    data.indices.forEach { i ->
                        val px = x(i); val py = y(serie.values[i], serie.max)
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(path, color = serie.color, style = Stroke(2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    data.indices.forEach { i ->
                        drawCircle(serie.color, radius = 3.5f, center = Offset(x(i), y(serie.values[i], serie.max)))
                    }
                }

                if (data.size > 1) {
                    val step = (data.size - 1) / 6.coerceAtMost(data.size - 1).coerceAtLeast(1)
                    (data.indices step step).forEach { i ->
                        drawCircle(Color(0xFF616161), radius = 2f, center = Offset(x(i), t + ch + 8f))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LegendDot(colors.orcamentos, "Orç."); LegendDot(colors.finalized, "Fin.")
                LegendDot(colors.visitas, "Vis."); LegendDot(colors.clientes, "Cli.")
                LegendDot(colors.lucro, "Lucro")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) { drawCircle(color) }
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
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
                            DetailRow(v.nomeCliente, v.endereco, "R$ ${"%.2f".format(v.valor)} - ${DateUtils.formatDate(v.data)}")
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
