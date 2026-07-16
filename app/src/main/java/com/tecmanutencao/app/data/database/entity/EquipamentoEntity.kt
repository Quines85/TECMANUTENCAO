package com.tecmanutencao.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tecmanutencao.app.domain.model.Equipamento
import com.tecmanutencao.app.domain.model.TipoMaquina

@Entity(
    tableName = "equipamentos",
    foreignKeys = [ForeignKey(
        entity = OrcamentoEntity::class,
        parentColumns = ["id"],
        childColumns = ["orcamentoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("orcamentoId")]
)
data class EquipamentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipoMaquina: String = TipoMaquina.DESKTOP.name,
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val problemaInformado: String = "",
    val observacoesTecnico: String = "",
    val orcamentoId: Long = 0
)

fun EquipamentoEntity.toDomain(): Equipamento = Equipamento(
    id = id,
    tipoMaquina = try { TipoMaquina.valueOf(tipoMaquina) } catch (e: Exception) { TipoMaquina.DESKTOP },
    marca = marca, modelo = modelo, numeroSerie = numeroSerie,
    problemaInformado = problemaInformado, observacoesTecnico = observacoesTecnico,
    orcamentoId = orcamentoId
)

fun Equipamento.toEntity(): EquipamentoEntity = EquipamentoEntity(
    id = id, tipoMaquina = tipoMaquina.name,
    marca = marca, modelo = modelo, numeroSerie = numeroSerie,
    problemaInformado = problemaInformado, observacoesTecnico = observacoesTecnico,
    orcamentoId = orcamentoId
)
