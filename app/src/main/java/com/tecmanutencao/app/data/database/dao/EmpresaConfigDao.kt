package com.tecmanutencao.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tecmanutencao.app.data.database.entity.EmpresaConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpresaConfigDao {

    @Query("SELECT * FROM empresa_config WHERE id = 1")
    fun getConfig(): Flow<EmpresaConfigEntity?>

    @Query("SELECT * FROM empresa_config WHERE id = 1")
    suspend fun getConfigOnce(): EmpresaConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: EmpresaConfigEntity)
}
