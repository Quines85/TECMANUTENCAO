package com.tecmanutencao.app.domain.model

data class Equipamento(
    val id: Long = 0,
    val tipoMaquina: TipoMaquina = TipoMaquina.DESKTOP,
    val marca: String = "",
    val modelo: String = "",
    val numeroSerie: String = "",
    val problemaInformado: String = "",
    val observacoesTecnico: String = "",
    val orcamentoId: Long = 0
)
