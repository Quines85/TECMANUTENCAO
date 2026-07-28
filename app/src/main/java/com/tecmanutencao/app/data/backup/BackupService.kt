package com.tecmanutencao.app.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.EmpresaConfigRepository
import com.tecmanutencao.app.data.repository.EquipamentoRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository
import com.tecmanutencao.app.data.repository.VisitaRepository
import com.tecmanutencao.app.domain.model.Cliente
import com.tecmanutencao.app.domain.model.EmpresaConfig
import com.tecmanutencao.app.domain.model.Equipamento
import com.tecmanutencao.app.domain.model.FormaPagamento
import com.tecmanutencao.app.domain.model.Orcamento
import com.tecmanutencao.app.domain.model.StatusOrcamento
import com.tecmanutencao.app.domain.model.StatusVisita
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

    private const val BACKUP_VERSION = 3

    data class BackupData(
        val empresaConfig: EmpresaConfig?,
        val clientes: List<Cliente>,
        val orcamentos: List<Orcamento>,
        val equipamentos: List<Equipamento>,
        val visitas: List<Visita>
    )

    data class ExportResult(
        val file: File,
        val publicPath: String?
    )

    suspend fun exportBackup(
        context: Context,
        clienteRepo: ClienteRepository,
        orcamentoRepo: OrcamentoRepository,
        equipamentoRepo: EquipamentoRepository,
        visitaRepo: VisitaRepository,
        empresaConfigRepo: EmpresaConfigRepository
    ): ExportResult {
        val empresaConfig = empresaConfigRepo.getConfigOnce()
        val clientes = clienteRepo.getAllClientes().first()
        val orcamentos = orcamentoRepo.getAllOrcamentos().first()
        val equipamentos = equipamentoRepo.getAllEquipamentos().first()
        val visitas = visitaRepo.getAllVisitas().first()

        val json = buildJson(empresaConfig, clientes, orcamentos, equipamentos, visitas)

        val dir = File(context.filesDir, Constants.BACKUP_DIR)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, Constants.BACKUP_FILE_NAME)
        file.writeText(json.toString(2))

        val publicPath = saveToDownloads(context, file)

        return ExportResult(file, publicPath)
    }

    private fun saveToDownloads(context: Context, source: File): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, Constants.BACKUP_FILE_NAME)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        source.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    "$downloadsDir/${Constants.BACKUP_FILE_NAME}"
                } else null
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dest = File(downloadsDir, Constants.BACKUP_FILE_NAME)
                source.copyTo(dest, overwrite = true)
                dest.absolutePath
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        clienteRepo: ClienteRepository,
        orcamentoRepo: OrcamentoRepository,
        equipamentoRepo: EquipamentoRepository,
        visitaRepo: VisitaRepository,
        empresaConfigRepo: EmpresaConfigRepository
    ): ImportResult {
        val json = context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        } ?: return ImportResult.Error("Não foi possível ler o arquivo")

        return try {
            val root = JSONObject(json)
            val version = root.optInt("version", 0)
            if (version < 1 || version > BACKUP_VERSION) {
                return ImportResult.Error("Versão do backup incompatível: $version")
            }

            val data = parseJson(root)

            var imported = 0

            if (data.empresaConfig != null) {
                empresaConfigRepo.saveConfig(data.empresaConfig.copy(id = 1))
                imported++
            }

            val hasIds = version >= 3
            val clienteIdMap = mutableMapOf<Long, Long>()
            for (cliente in data.clientes) {
                val oldId = cliente.id
                val newId = clienteRepo.saveCliente(cliente.copy(id = 0))
                if (hasIds) clienteIdMap[oldId] = newId
                imported++
            }

            val orcamentoIdMap = mutableMapOf<Long, Long>()
            val orcamentosToUpdate = mutableListOf<Pair<Long, Long>>()
            for (orcamento in data.orcamentos) {
                val oldId = orcamento.id
                val adjustedClienteId = if (hasIds) clienteIdMap[orcamento.clienteId] ?: 0 else 0
                val adjustedOrcamento = orcamento.copy(
                    id = 0,
                    clienteId = adjustedClienteId
                )
                val newId = orcamentoRepo.saveOrcamento(adjustedOrcamento)
                if (hasIds) {
                    orcamentoIdMap[oldId] = newId
                    if (orcamento.equipamentoId > 0) {
                        orcamentosToUpdate.add(newId to orcamento.equipamentoId)
                    }
                }
                imported++
            }

            val equipamentoIdMap = mutableMapOf<Long, Long>()
            for (equipamento in data.equipamentos) {
                val oldId = equipamento.id
                val adjustedOrcamentoId = if (hasIds) orcamentoIdMap[equipamento.orcamentoId] ?: 0 else 0
                val newId = equipamentoRepo.saveEquipamento(equipamento.copy(
                    id = 0,
                    orcamentoId = adjustedOrcamentoId
                ))
                if (hasIds) equipamentoIdMap[oldId] = newId
                imported++
            }

            if (hasIds) {
                for ((newOrcamentoId, oldEquipamentoId) in orcamentosToUpdate) {
                    val newEquipamentoId = equipamentoIdMap[oldEquipamentoId]
                    if (newEquipamentoId != null) {
                        val existing = orcamentoRepo.getOrcamentoById(newOrcamentoId)
                        if (existing != null) {
                            orcamentoRepo.updateOrcamento(existing.copy(equipamentoId = newEquipamentoId))
                        }
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
        empresaConfig: EmpresaConfig?,
        clientes: List<Cliente>,
        orcamentos: List<Orcamento>,
        equipamentos: List<Equipamento>,
        visitas: List<Visita>
    ): JSONObject {
        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("exportDate", System.currentTimeMillis())

            put("empresaConfig", empresaConfig?.toJson() ?: JSONObject.NULL)
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
        val empresaConfig = if (!root.isNull("empresaConfig")) {
            empresaConfigFromJson(root.optJSONObject("empresaConfig"))
        } else null

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

        return BackupData(empresaConfig, clientes, orcamentos, equipamentos, visitas)
    }

    private fun EmpresaConfig.toJson() = JSONObject().apply {
        put("nomeEmpresa", nomeEmpresa)
        put("cnpj", cnpj)
        put("telefone", telefone)
        put("whatsapp", whatsapp)
        put("email", email)
        put("endereco", endereco)
        put("cidade", cidade)
        put("estado", estado)
        put("cep", cep)
    }

    private fun Cliente.toJson() = JSONObject().apply {
        put("id", id)
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
        put("id", id)
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
        put("id", id)
        put("tipoMaquina", tipoMaquina.name)
        put("marca", marca)
        put("modelo", modelo)
        put("numeroSerie", numeroSerie)
        put("problemaInformado", problemaInformado)
        put("observacoesTecnico", observacoesTecnico)
        put("orcamentoId", orcamentoId)
    }

    private fun Visita.toJson() = JSONObject().apply {
        put("id", id)
        put("data", data)
        put("nomeCliente", nomeCliente)
        put("endereco", endereco)
        put("problemaRelatado", problemaRelatado)
        put("solucao", solucao)
        put("valor", valor)
        put("observacoes", observacoes)
        put("status", status.name)
    }

    private fun empresaConfigFromJson(obj: JSONObject?) = obj?.let {
        EmpresaConfig(
            nomeEmpresa = it.optString("nomeEmpresa", ""),
            cnpj = it.optString("cnpj", ""),
            telefone = it.optString("telefone", ""),
            whatsapp = it.optString("whatsapp", ""),
            email = it.optString("email", ""),
            endereco = it.optString("endereco", ""),
            cidade = it.optString("cidade", ""),
            estado = it.optString("estado", ""),
            cep = it.optString("cep", "")
        )
    }

    private fun clienteFromJson(obj: JSONObject) = Cliente(
        id = obj.optLong("id", 0),
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
        id = obj.optLong("id", 0),
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
        id = obj.optLong("id", 0),
        tipoMaquina = try { TipoMaquina.valueOf(obj.optString("tipoMaquina", "")) } catch (e: Exception) { TipoMaquina.DESKTOP },
        marca = obj.optString("marca", ""),
        modelo = obj.optString("modelo", ""),
        numeroSerie = obj.optString("numeroSerie", ""),
        problemaInformado = obj.optString("problemaInformado", ""),
        observacoesTecnico = obj.optString("observacoesTecnico", ""),
        orcamentoId = obj.optLong("orcamentoId", 0)
    )

    private fun visitaFromJson(obj: JSONObject) = Visita(
        id = obj.optLong("id", 0),
        data = obj.optLong("data", System.currentTimeMillis()),
        nomeCliente = obj.optString("nomeCliente", ""),
        endereco = obj.optString("endereco", ""),
        problemaRelatado = obj.optString("problemaRelatado", ""),
        solucao = obj.optString("solucao", ""),
        valor = obj.optDouble("valor", 0.0),
        observacoes = obj.optString("observacoes", ""),
        status = try { StatusVisita.valueOf(obj.optString("status", "")) } catch (e: Exception) { StatusVisita.AGENDADA }
    )
}
