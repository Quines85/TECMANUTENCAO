package com.tecmanutencao.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tecmanutencao.app.data.database.entity.ClienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {

    @Query("SELECT * FROM clientes ORDER BY nomeCompleto ASC")
    fun getAllClientes(): Flow<List<ClienteEntity>>

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun getClienteById(id: Long): ClienteEntity?

    @Query("SELECT * FROM clientes WHERE nomeCompleto LIKE '%' || :query || '%' OR telefone LIKE '%' || :query || '%' OR cpfCnpj LIKE '%' || :query || '%' ORDER BY nomeCompleto ASC")
    fun searchClientes(query: String): Flow<List<ClienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCliente(cliente: ClienteEntity): Long

    @Update
    suspend fun updateCliente(cliente: ClienteEntity)

    @Delete
    suspend fun deleteCliente(cliente: ClienteEntity)

    @Query("SELECT COUNT(*) FROM clientes WHERE dataCadastro BETWEEN :startDate AND :endDate")
    suspend fun getNewClientCountBetween(startDate: Long, endDate: Long): Int

    @Query("SELECT * FROM clientes WHERE dataCadastro BETWEEN :startDate AND :endDate ORDER BY dataCadastro DESC")
    suspend fun getClientesBetween(startDate: Long, endDate: Long): List<ClienteEntity>
}
