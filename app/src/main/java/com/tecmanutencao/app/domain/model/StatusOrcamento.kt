package com.tecmanutencao.app.domain.model

enum class StatusOrcamento(val descricao: String) {
    ABERTO("Aberto"),
    AGUARDANDO_APROVACAO("Aguardando Aprovação"),
    APROVADO("Aprovado"),
    RECUSADO("Recusado"),
    FINALIZADO("Finalizado")
}
