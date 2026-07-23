package com.tecmanutencao.app.data.repository

import com.tecmanutencao.app.data.database.dao.VisitaDao
import com.tecmanutencao.app.data.database.entity.toDomain
import com.tecmanutencao.app.data.database.entity.toEntity
import com.tecmanutencao.app.domain.model.Visita
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VisitaRepository(private val dao: VisitaDao) {

    fun getAllVisitas(): Flow<List<Visita>> =
        dao.getAllVisitas().map { list -> list.map { it.toDomain() } }

    fun searchVisitas(query: String): Flow<List<Visita>> =
        dao.searchVisitas(query).map { list -> list.map { it.toDomain() } }

    suspend fun getVisitaById(id: Long): Visita? =
        dao.getVisitaById(id)?.toDomain()

    suspend fun getTotalVisitas(): Double =
        dao.getTotalVisitas()

    suspend fun saveVisita(visita: Visita): Long =
        dao.insertVisita(visita.toEntity())

    suspend fun updateVisita(visita: Visita) =
        dao.updateVisita(visita.toEntity())

    suspend fun deleteVisita(visita: Visita) =
        dao.deleteVisita(visita.toEntity())
}
