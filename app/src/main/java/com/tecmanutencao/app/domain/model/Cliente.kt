package com.tecmanutencao.app.domain.model

data class Cliente(
    val id: Long = 0,
    val nomeCompleto: String = "",
    val cpfCnpj: String = "",
    val telefone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val estado: String = "",
    val cep: String = "",
    val observacoes: String = "",
    val dataCadastro: Long = System.currentTimeMillis()
)
