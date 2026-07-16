package com.tecmanutencao.app.data.repository

import com.tecmanutencao.app.data.database.dao.ClienteDao
import com.tecmanutencao.app.data.database.entity.toDomain
import com.tecmanutencao.app.data.database.entity.toEntity
import com.tecmanutencao.app.domain.model.Cliente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClienteRepository(private val dao: ClienteDao) {

    fun getAllClientes(): Flow<List<Cliente>> =
        dao.getAllClientes().map { list -> list.map { it.toDomain() } }

    fun searchClientes(query: String): Flow<List<Cliente>> =
        dao.searchClientes(query).map { list -> list.map { it.toDomain() } }

    suspend fun getClienteById(id: Long): Cliente? =
        dao.getClienteById(id)?.toDomain()

    suspend fun saveCliente(cliente: Cliente): Long =
        dao.insertCliente(cliente.toEntity())

    suspend fun updateCliente(cliente: Cliente) =
        dao.updateCliente(cliente.toEntity())

    suspend fun deleteCliente(cliente: Cliente) =
        dao.deleteCliente(cliente.toEntity())
}
