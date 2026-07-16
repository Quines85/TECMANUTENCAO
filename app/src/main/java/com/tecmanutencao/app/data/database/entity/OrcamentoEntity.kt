package com.tecmanutencao.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tecmanutencao.app.domain.model.FormaPagamento
import com.tecmanutencao.app.domain.model.Orcamento
import com.tecmanutencao.app.domain.model.StatusOrcamento

@Entity(
    tableName = "orcamentos",
    foreignKeys = [ForeignKey(
        entity = ClienteEntity::class,
        parentColumns = ["id"],
        childColumns = ["clienteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("clienteId")]
)
data class OrcamentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numeroOrcamento: String = "",
    val data: Long = System.currentTimeMillis(),
    val clienteId: Long = 0,
    val descricaoServico: String = "",
    val valorServico: Double = 0.0,
    val formaPagamento: String = FormaPagamento.PIX.name,
    val observacoes: String = "",
    val status: String = StatusOrcamento.ABERTO.name,
    val equipamentoId: Long = 0
)

fun OrcamentoEntity.toDomain(): Orcamento = Orcamento(
    id = id, numeroOrcamento = numeroOrcamento, data = data,
    clienteId = clienteId, descricaoServico = descricaoServico,
    valorServico = valorServico,
    formaPagamento = try { FormaPagamento.valueOf(formaPagamento) } catch (e: Exception) { FormaPagamento.PIX },
    observacoes = observacoes,
    status = try { StatusOrcamento.valueOf(status) } catch (e: Exception) { StatusOrcamento.ABERTO },
    equipamentoId = equipamentoId
)

fun Orcamento.toEntity(): OrcamentoEntity = OrcamentoEntity(
    id = id, numeroOrcamento = numeroOrcamento, data = data,
    clienteId = clienteId, descricaoServico = descricaoServico,
    valorServico = valorServico, formaPagamento = formaPagamento.name,
    observacoes = observacoes, status = status.name,
    equipamentoId = equipamentoId
)
