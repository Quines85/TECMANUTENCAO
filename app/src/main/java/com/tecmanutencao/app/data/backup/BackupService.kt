package com.tecmanutencao.app.data.backup

import android.content.Context
import android.net.Uri
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.EquipamentoRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository
import com.tecmanutencao.app.data.repository.VisitaRepository
import com.tecmanutencao.app.domain.model.Cliente
import com.tecmanutencao.app.domain.model.Equipamento
import com.tecmanutencao.app.domain.model.FormaPagamento
import com.tecmanutencao.app.domain.model.Orcamento
import com.tecmanutencao.app.domain.model.StatusOrcamento
import com.tecmanutencao.app.domain.model.TipoMaquina
import com.tecmanutencao.app.domain.model.Visita
import com.tecmanutencao.app.util.Constants
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object BackupService {

    private const val BACKUP_VERSION = 1

    data class BackupData(
        val clientes: List<Cliente>,
        val orcamentos: List<Orcamento>,
        val equipamentos: List<Equipamento>,
        val visitas: List<Visita>
    )

    suspend fun exportBackup(
        context: Context,
        clienteRepo: ClienteRepository,
        orcamentoRepo: OrcamentoRepository,
        equipamentoRepo: EquipamentoRepository,
        visitaRepo: VisitaRepository
    ): File {
        val clientes = clienteRepo.getAllClientes().first()
        val orcamentos = orcamentoRepo.getAllOrcamentos().first()
        val equipamentos = equipamentoRepo.getAllEquipamentos().first()
        val visitas = visitaRepo.getAllVisitas().first()

        val json = buildJson(clientes, orcamentos, equipamentos, visitas)

        val dir = File(context.filesDir, Constants.BACKUP_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, Constants.BACKUP_FILE_NAME)
        file.writeText(json.toString(2))

        return file
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        clienteRepo: ClienteRepository,
        orcamentoRepo: OrcamentoRepository,
        equipamentoRepo: EquipamentoRepository,
        visitaRepo: VisitaRepository
    ): ImportResult {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        } ?: return ImportResult.Error("Não foi possível ler o arquivo")

        return try {
            val root = JSONObject(json)
            val version = root.optInt("version", 0)
            if (version != BACKUP_VERSION) {
                return ImportResult.Error("Versão do backup incompatível: $version")
            }

            val data = parseJson(root)

            var imported = 0

            val clienteIdMap = mutableMapOf<Long, Long>()
            for (cliente in data.clientes) {
                val newId = clienteRepo.saveCliente(cliente.copy(id = 0))
                clienteIdMap[cliente.id] = newId
                imported++
            }

            val orcamentoIdMap = mutableMapOf<Long, Long>()
            val orcamentosToUpdate = mutableListOf<Pair<Long, Long>>()
            for (orcamento in data.orcamentos) {
                val adjustedOrcamento = orcamento.copy(
                    id = 0,
                    clienteId = clienteIdMap[orcamento.clienteId] ?: 0
                )
                val newId = orcamentoRepo.saveOrcamento(adjustedOrcamento)
                orcamentoIdMap[orcamento.id] = newId
                if (orcamento.equipamentoId > 0) {
                    orcamentosToUpdate.add(newId to orcamento.equipamentoId)
                }
                imported++
            }

            val equipamentoIdMap = mutableMapOf<Long, Long>()
            for (equipamento in data.equipamentos) {
                val newId = equipamentoRepo.saveEquipamento(equipamento.copy(
                    id = 0,
                    orcamentoId = orcamentoIdMap[equipamento.orcamentoId] ?: 0
                ))
                equipamentoIdMap[equipamento.id] = newId
                imported++
            }

            for ((newOrcamentoId, oldEquipamentoId) in orcamentosToUpdate) {
                val newEquipamentoId = equipamentoIdMap[oldEquipamentoId]
                if (newEquipamentoId != null) {
                    val existing = orcamentoRepo.getOrcamentoById(newOrcamentoId)
                    if (existing != null) {
                        orcamentoRepo.updateOrcamento(existing.copy(equipamentoId = newEquipamentoId))
                    }
                }
            }

            for (visita in data.visitas) {
                visitaRepo.saveVisita(visita.copy(id = 0))
                imported++
            }

            ImportResult.Success(imported)
        } catch (e: Exception) {
            ImportResult.Error("Erro ao importar: ${e.message}")
        }
    }

    sealed class ImportResult {
        data class Success(val count: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    private fun buildJson(
        clientes: List<Cliente>,
        orcamentos: List<Orcamento>,
        equipamentos: List<Equipamento>,
        visitas: List<Visita>
    ): JSONObject {
        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("exportDate", System.currentTimeMillis())

            put("clientes", JSONArray().apply {
                clientes.forEach { put(it.toJson()) }
            })
            put("orcamentos", JSONArray().apply {
                orcamentos.forEach { put(it.toJson()) }
            })
            put("equipamentos", JSONArray().apply {
                equipamentos.forEach { put(it.toJson()) }
            })
            put("visitas", JSONArray().apply {
                visitas.forEach { put(it.toJson()) }
            })
        }
    }

    private fun parseJson(root: JSONObject): BackupData {
        val clientes = mutableListOf<Cliente>()
        val orcamentos = mutableListOf<Orcamento>()
        val equipamentos = mutableListOf<Equipamento>()
        val visitas = mutableListOf<Visita>()

        val clientesArr = root.optJSONArray("clientes") ?: JSONArray()
        for (i in 0 until clientesArr.length()) {
            clientes.add(clienteFromJson(clientesArr.getJSONObject(i)))
        }

        val orcamentosArr = root.optJSONArray("orcamentos") ?: JSONArray()
        for (i in 0 until orcamentosArr.length()) {
            orcamentos.add(orcamentoFromJson(orcamentosArr.getJSONObject(i)))
        }

        val equipamentosArr = root.optJSONArray("equipamentos") ?: JSONArray()
        for (i in 0 until equipamentosArr.length()) {
            equipamentos.add(equipamentoFromJson(equipamentosArr.getJSONObject(i)))
        }

        val visitasArr = root.optJSONArray("visitas") ?: JSONArray()
        for (i in 0 until visitasArr.length()) {
            visitas.add(visitaFromJson(visitasArr.getJSONObject(i)))
        }

        return BackupData(clientes, orcamentos, equipamentos, visitas)
    }

    private fun Cliente.toJson() = JSONObject().apply {
        put("nomeCompleto", nomeCompleto)
        put("cpfCnpj", cpfCnpj)
        put("telefone", telefone)
        put("whatsapp", whatsapp)
        put("email", email)
        put("endereco", endereco)
        put("cidade", cidade)
        put("estado", estado)
        put("cep", cep)
        put("observacoes", observacoes)
        put("dataCadastro", dataCadastro)
    }

    private fun Orcamento.toJson() = JSONObject().apply {
        put("numeroOrcamento", numeroOrcamento)
        put("data", data)
        put("clienteId", clienteId)
        put("descricaoServico", descricaoServico)
        put("valorServico", valorServico)
        put("formaPagamento", formaPagamento.name)
        put("observacoes", observacoes)
        put("status", status.name)
        put("equipamentoId", equipamentoId)
    }

    private fun Equipamento.toJson() = JSONObject().apply {
        put("tipoMaquina", tipoMaquina.name)
        put("marca", marca)
        put("modelo", modelo)
        put("numeroSerie", numeroSerie)
        put("problemaInformado", problemaInformado)
        put("observacoesTecnico", observacoesTecnico)
        put("orcamentoId", orcamentoId)
    }

    private fun Visita.toJson() = JSONObject().apply {
        put("data", data)
        put("nomeCliente", nomeCliente)
        put("endereco", endereco)
        put("problemaRelatado", problemaRelatado)
        put("solucao", solucao)
        put("valor", valor)
        put("observacoes", observacoes)
    }

    private fun clienteFromJson(obj: JSONObject) = Cliente(
        nomeCompleto = obj.optString("nomeCompleto", ""),
        cpfCnpj = obj.optString("cpfCnpj", ""),
        telefone = obj.optString("telefone", ""),
        whatsapp = obj.optString("whatsapp", ""),
        email = obj.optString("email", ""),
        endereco = obj.optString("endereco", ""),
        cidade = obj.optString("cidade", ""),
        estado = obj.optString("estado", ""),
        cep = obj.optString("cep", ""),
        observacoes = obj.optString("observacoes", ""),
        dataCadastro = obj.optLong("dataCadastro", System.currentTimeMillis())
    )

    private fun orcamentoFromJson(obj: JSONObject) = Orcamento(
        numeroOrcamento = obj.optString("numeroOrcamento", ""),
        data = obj.optLong("data", System.currentTimeMillis()),
        clienteId = obj.optLong("clienteId", 0),
        descricaoServico = obj.optString("descricaoServico", ""),
        valorServico = obj.optDouble("valorServico", 0.0),
        formaPagamento = try { FormaPagamento.valueOf(obj.optString("formaPagamento", "")) } catch (e: Exception) { FormaPagamento.PIX },
        observacoes = obj.optString("observacoes", ""),
        status = try { StatusOrcamento.valueOf(obj.optString("status", "")) } catch (e: Exception) { StatusOrcamento.ABERTO },
        equipamentoId = obj.optLong("equipamentoId", 0)
    )

    private fun equipamentoFromJson(obj: JSONObject) = Equipamento(
        tipoMaquina = try { TipoMaquina.valueOf(obj.optString("tipoMaquina", "")) } catch (e: Exception) { TipoMaquina.DESKTOP },
        marca = obj.optString("marca", ""),
        modelo = obj.optString("modelo", ""),
        numeroSerie = obj.optString("numeroSerie", ""),
        problemaInformado = obj.optString("problemaInformado", ""),
        observacoesTecnico = obj.optString("observacoesTecnico", ""),
        orcamentoId = obj.optLong("orcamentoId", 0)
    )

    private fun visitaFromJson(obj: JSONObject) = Visita(
        data = obj.optLong("data", System.currentTimeMillis()),
        nomeCliente = obj.optString("nomeCliente", ""),
        endereco = obj.optString("endereco", ""),
        problemaRelatado = obj.optString("problemaRelatado", ""),
        solucao = obj.optString("solucao", ""),
        valor = obj.optDouble("valor", 0.0),
        observacoes = obj.optString("observacoes", "")
    )
}
