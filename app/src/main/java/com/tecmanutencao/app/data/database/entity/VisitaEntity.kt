package com.tecmanutencao.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecmanutencao.app.domain.model.Visita

@Entity(tableName = "visitas")
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val data: Long = System.currentTimeMillis(),
    val nomeCliente: String = "",
    val endereco: String = "",
    val problemaRelatado: String = "",
    val solucao: String = "",
    val valor: Double = 0.0,
    val observacoes: String = ""
)

fun VisitaEntity.toDomain(): Visita = Visita(
    id = id, data = data, nomeCliente = nomeCliente,
    endereco = endereco, problemaRelatado = problemaRelatado,
    solucao = solucao, valor = valor, observacoes = observacoes
)

fun Visita.toEntity(): VisitaEntity = VisitaEntity(
    id = id, data = data, nomeCliente = nomeCliente,
    endereco = endereco, problemaRelatado = problemaRelatado,
    solucao = solucao, valor = valor, observacoes = observacoes
)
