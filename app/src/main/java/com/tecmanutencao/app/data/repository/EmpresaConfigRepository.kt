package com.tecmanutencao.app.data.repository

import com.tecmanutencao.app.data.database.dao.EmpresaConfigDao
import com.tecmanutencao.app.data.database.entity.toDomain
import com.tecmanutencao.app.data.database.entity.toEntity
import com.tecmanutencao.app.domain.model.EmpresaConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EmpresaConfigRepository(private val dao: EmpresaConfigDao) {

    fun getConfig(): Flow<EmpresaConfig?> =
        dao.getConfig().map { it?.toDomain() }

    suspend fun getConfigOnce(): EmpresaConfig? =
        dao.getConfigOnce()?.toDomain()

    suspend fun saveConfig(config: EmpresaConfig) =
        dao.saveConfig(config.toEntity())
}
