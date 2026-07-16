package com.tecmanutencao.app.data.repository

import com.tecmanutencao.app.data.database.dao.OrcamentoDao
import com.tecmanutencao.app.data.database.entity.toDomain
import com.tecmanutencao.app.data.database.entity.toEntity
import com.tecmanutencao.app.domain.model.Orcamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrcamentoRepository(private val dao: OrcamentoDao) {

    fun getAllOrcamentos(): Flow<List<Orcamento>> =
        dao.getAllOrcamentos().map { list -> list.map { it.toDomain() } }

    fun getOrcamentosByClienteId(clienteId: Long): Flow<List<Orcamento>> =
        dao.getOrcamentosByClienteId(clienteId).map { list -> list.map { it.toDomain() } }

    suspend fun getOrcamentoById(id: Long): Orcamento? =
        dao.getOrcamentoById(id)?.toDomain()

    suspend fun saveOrcamento(orcamento: Orcamento): Long =
        dao.insertOrcamento(orcamento.toEntity())

    suspend fun updateOrcamento(orcamento: Orcamento) =
        dao.updateOrcamento(orcamento.toEntity())

    suspend fun deleteOrcamento(orcamento: Orcamento) =
        dao.deleteOrcamento(orcamento.toEntity())

    suspend fun getOrcamentoCount(): Int =
        dao.getOrcamentoCount()
}
