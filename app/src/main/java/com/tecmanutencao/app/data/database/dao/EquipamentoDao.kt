package com.tecmanutencao.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tecmanutencao.app.data.database.entity.EquipamentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipamentoDao {

    @Query("SELECT * FROM equipamentos")
    fun getAllEquipamentos(): Flow<List<EquipamentoEntity>>

    @Query("SELECT * FROM equipamentos WHERE orcamentoId = :orcamentoId")
    fun getEquipamentosByOrcamentoId(orcamentoId: Long): Flow<List<EquipamentoEntity>>

    @Query("SELECT * FROM equipamentos WHERE id = :id")
    suspend fun getEquipamentoById(id: Long): EquipamentoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipamento(equipamento: EquipamentoEntity): Long

    @Update
    suspend fun updateEquipamento(equipamento: EquipamentoEntity)

    @Delete
    suspend fun deleteEquipamento(equipamento: EquipamentoEntity)

    @Query("DELETE FROM equipamentos WHERE orcamentoId = :orcamentoId")
    suspend fun deleteByOrcamentoId(orcamentoId: Long)
}
