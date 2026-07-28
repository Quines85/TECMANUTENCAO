package com.tecmanutencao.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tecmanutencao.app.data.database.entity.VisitaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {

    @Query("SELECT * FROM visitas ORDER BY data DESC")
    fun getAllVisitas(): Flow<List<VisitaEntity>>

    @Query("SELECT * FROM visitas WHERE id = :id")
    suspend fun getVisitaById(id: Long): VisitaEntity?

    @Query("SELECT * FROM visitas WHERE nomeCliente LIKE '%' || :query || '%' OR endereco LIKE '%' || :query || '%' ORDER BY data DESC")
    fun searchVisitas(query: String): Flow<List<VisitaEntity>>

    @Query("SELECT SUM(valor) FROM visitas")
    suspend fun getTotalVisitas(): Double

    @Query("SELECT COUNT(*) FROM visitas WHERE data BETWEEN :startDate AND :endDate")
    suspend fun getVisitaCountBetween(startDate: Long, endDate: Long): Int

    @Query("SELECT COALESCE(SUM(valor), 0) FROM visitas WHERE data BETWEEN :startDate AND :endDate AND status = 'FINALIZADA'")
    suspend fun getVisitaProfitBetween(startDate: Long, endDate: Long): Double

    @Query("SELECT * FROM visitas WHERE data BETWEEN :startDate AND :endDate ORDER BY data DESC")
    suspend fun getVisitasBetween(startDate: Long, endDate: Long): List<VisitaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisita(visita: VisitaEntity): Long

    @Update
    suspend fun updateVisita(visita: VisitaEntity)

    @Delete
    suspend fun deleteVisita(visita: VisitaEntity)
}
