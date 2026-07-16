package com.tecmanutencao.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tecmanutencao.app.ui.cliente.ClienteFormScreen
import com.tecmanutencao.app.ui.cliente.ClienteListScreen
import com.tecmanutencao.app.ui.config.ConfigScreen
import com.tecmanutencao.app.ui.home.HomeScreen
import com.tecmanutencao.app.ui.orcamento.OrcamentoDetailScreen
import com.tecmanutencao.app.ui.orcamento.OrcamentoFormScreen
import com.tecmanutencao.app.ui.orcamento.OrcamentoListScreen

object Routes {
    const val HOME = "home"
    const val CLIENTE_LIST = "cliente_list"
    const val CLIENTE_FORM = "cliente_form/{clienteId}"
    const val ORCAMENTO_FORM = "orcamento_form/{orcamentoId}"
    const val ORCAMENTO_LIST = "orcamento_list"
    const val ORCAMENTO_DETAIL = "orcamento_detail/{orcamentoId}"
    const val CONFIG = "config"

    fun clienteForm(clienteId: Long = 0L) = "cliente_form/$clienteId"
    fun orcamentoForm(orcamentoId: Long = 0L) = "orcamento_form/$orcamentoId"
    fun orcamentoDetail(orcamentoId: Long) = "orcamento_detail/$orcamentoId"
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
                onNavigateToConfig = { navController.navigate(Routes.CONFIG) }
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
    }
}
