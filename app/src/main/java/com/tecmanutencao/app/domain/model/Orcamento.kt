package com.tecmanutencao.app.domain.model

data class Orcamento(
    val id: Long = 0,
    val numeroOrcamento: String = "",
    val data: Long = System.currentTimeMillis(),
    val clienteId: Long = 0,
    val descricaoServico: String = "",
    val valorServico: Double = 0.0,
    val formaPagamento: FormaPagamento = FormaPagamento.PIX,
    val observacoes: String = "",
    val status: StatusOrcamento = StatusOrcamento.ABERTO,
    val equipamentoId: Long = 0
)
