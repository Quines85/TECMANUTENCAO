package com.tecmanutencao.app.domain.model

data class Visita(
    val id: Long = 0,
    val data: Long = System.currentTimeMillis(),
    val nomeCliente: String = "",
    val endereco: String = "",
    val problemaRelatado: String = "",
    val solucao: String = "",
    val valor: Double = 0.0,
    val observacoes: String = "",
    val status: StatusVisita = StatusVisita.AGENDADA
)
