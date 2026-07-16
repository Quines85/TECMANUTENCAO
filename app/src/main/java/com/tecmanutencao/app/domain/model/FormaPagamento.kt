package com.tecmanutencao.app.domain.model

enum class FormaPagamento(val descricao: String) {
    PIX("PIX"),
    CARTAO_DEBITO("Cartão de Débito"),
    CARTAO_CREDITO("Cartão de Crédito"),
    DINHEIRO("Dinheiro")
}
