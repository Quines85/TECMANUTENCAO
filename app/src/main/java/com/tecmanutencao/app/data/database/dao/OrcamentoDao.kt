package com.tecmanutencao.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tecmanutencao.app.data.database.entity.OrcamentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrcamentoDao {

    @Query("SELECT * FROM orcamentos ORDER BY data DESC")
    fun getAllOrcamentos(): Flow<List<OrcamentoEntity>>

    @Query("SELECT * FROM orcamentos WHERE id = :id")
    suspend fun getOrcamentoById(id: Long): OrcamentoEntity?

    @Query("SELECT * FROM orcamentos WHERE clienteId = :clienteId ORDER BY data DESC")
    fun getOrcamentosByClienteId(clienteId: Long): Flow<List<OrcamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrcamento(orcamento: OrcamentoEntity): Long

    @Update
    suspend fun updateOrcamento(orcamento: OrcamentoEntity)

    @Delete
    suspend fun deleteOrcamento(orcamento: OrcamentoEntity)

    @Query("SELECT COUNT(*) FROM orcamentos")
    suspend fun getOrcamentoCount(): Int
}
