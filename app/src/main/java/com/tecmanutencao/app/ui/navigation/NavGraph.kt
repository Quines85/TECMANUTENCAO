package com.tecmanutencao.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tecmanutencao.app.ui.backup.BackupScreen
import com.tecmanutencao.app.ui.cliente.ClienteFormScreen
import com.tecmanutencao.app.ui.cliente.ClienteListScreen
import com.tecmanutencao.app.ui.config.ConfigScreen
import com.tecmanutencao.app.ui.dashboard.DashboardScreen
import com.tecmanutencao.app.ui.home.HomeScreen
import com.tecmanutencao.app.ui.orcamento.OrcamentoDetailScreen
import com.tecmanutencao.app.ui.orcamento.OrcamentoFormScreen
import com.tecmanutencao.app.ui.orcamento.OrcamentoListScreen
import com.tecmanutencao.app.ui.visita.VisitaFormScreen
import com.tecmanutencao.app.ui.visita.VisitaListScreen

object Routes {
    const val HOME = "home"
    const val CLIENTE_LIST = "cliente_list"
    const val CLIENTE_FORM = "cliente_form/{clienteId}"
    const val ORCAMENTO_FORM = "orcamento_form/{orcamentoId}"
    const val ORCAMENTO_LIST = "orcamento_list"
    const val ORCAMENTO_DETAIL = "orcamento_detail/{orcamentoId}"
    const val CONFIG = "config"
    const val VISITA_LIST = "visita_list"
    const val VISITA_FORM = "visita_form/{visitaId}"
    const val BACKUP = "backup"
    const val DASHBOARD = "dashboard"

    fun clienteForm(clienteId: Long = 0L) = "cliente_form/$clienteId"
    fun orcamentoForm(orcamentoId: Long = 0L) = "orcamento_form/$orcamentoId"
    fun orcamentoDetail(orcamentoId: Long) = "orcamento_detail/$orcamentoId"
    fun visitaForm(visitaId: Long = 0L) = "visita_form/$visitaId"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToClientes = { navController.navigate(Routes.CLIENTE_LIST) },
                onNavigateToNovoOrcamento = { navController.navigate(Routes.orcamentoForm()) },
                onNavigateToOrcamentos = { navController.navigate(Routes.ORCAMENTO_LIST) },
                onNavigateToConfig = { navController.navigate(Routes.CONFIG) },
                onNavigateToVisitas = { navController.navigate(Routes.VISITA_LIST) },
                onNavigateToBackup = { navController.navigate(Routes.BACKUP) },
                onNavigateToDashboard = { navController.navigate(Routes.DASHBOARD) }
            )
        }

        composable(Routes.CLIENTE_LIST) {
            ClienteListScreen(
                onNavigateToForm = { navController.navigate(Routes.clienteForm(it)) },
                onNavigateToNovo = { navController.navigate(Routes.clienteForm()) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.CLIENTE_FORM,
            arguments = listOf(navArgument("clienteId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getLong("clienteId") ?: 0L
            ClienteFormScreen(
                clienteId = clienteId,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ORCAMENTO_FORM,
            arguments = listOf(navArgument("orcamentoId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val orcamentoId = backStackEntry.arguments?.getLong("orcamentoId") ?: 0L
            OrcamentoFormScreen(
                orcamentoId = orcamentoId,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ORCAMENTO_LIST) {
            OrcamentoListScreen(
                onNavigateToDetail = { navController.navigate(Routes.orcamentoDetail(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ORCAMENTO_DETAIL,
            arguments = listOf(navArgument("orcamentoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orcamentoId = backStackEntry.arguments?.getLong("orcamentoId") ?: 0L
            OrcamentoDetailScreen(
                orcamentoId = orcamentoId,
                onEdit = { navController.navigate(Routes.orcamentoForm(orcamentoId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CONFIG) {
            ConfigScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.VISITA_LIST) {
            VisitaListScreen(
                onNavigateToForm = { navController.navigate(Routes.visitaForm(it)) },
                onNavigateToNovo = { navController.navigate(Routes.visitaForm()) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VISITA_FORM,
            arguments = listOf(navArgument("visitaId") { type = NavType.LongType; defaultValue = 0L })
        ) { backStackEntry ->
            val visitaId = backStackEntry.arguments?.getLong("visitaId") ?: 0L
            VisitaFormScreen(
                visitaId = visitaId,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }
    }
}
