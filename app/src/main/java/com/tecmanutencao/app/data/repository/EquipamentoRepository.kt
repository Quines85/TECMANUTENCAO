package com.tecmanutencao.app.data.repository

import com.tecmanutencao.app.data.database.dao.EquipamentoDao
import com.tecmanutencao.app.data.database.entity.toDomain
import com.tecmanutencao.app.data.database.entity.toEntity
import com.tecmanutencao.app.domain.model.Equipamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EquipamentoRepository(private val dao: EquipamentoDao) {

    fun getEquipamentosByOrcamentoId(orcamentoId: Long): Flow<List<Equipamento>> =
        dao.getEquipamentosByOrcamentoId(orcamentoId).map { list -> list.map { it.toDomain() } }

    suspend fun getEquipamentoById(id: Long): Equipamento? =
        dao.getEquipamentoById(id)?.toDomain()

    suspend fun saveEquipamento(equipamento: Equipamento): Long =
        dao.insertEquipamento(equipamento.toEntity())

    suspend fun updateEquipamento(equipamento: Equipamento) =
        dao.updateEquipamento(equipamento.toEntity())

    suspend fun deleteEquipamento(equipamento: Equipamento) =
        dao.deleteEquipamento(equipamento.toEntity())

    suspend fun deleteByOrcamentoId(orcamentoId: Long) =
        dao.deleteByOrcamentoId(orcamentoId)
}
