package com.tecmanutencao.app.domain.model

data class EmpresaConfig(
    val id: Int = 1,
    val nomeEmpresa: String = "",
    val cnpj: String = "",
    val telefone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val estado: String = "",
    val cep: String = "",
    val logoPath: String = ""
)
