package com.tecmanutencao.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecmanutencao.app.domain.model.EmpresaConfig

@Entity(tableName = "empresa_config")
data class EmpresaConfigEntity(
    @PrimaryKey
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

fun EmpresaConfigEntity.toDomain(): EmpresaConfig = EmpresaConfig(
    id = id, nomeEmpresa = nomeEmpresa, cnpj = cnpj,
    telefone = telefone, whatsapp = whatsapp, email = email,
    endereco = endereco, cidade = cidade, estado = estado,
    cep = cep, logoPath = logoPath
)

fun EmpresaConfig.toEntity(): EmpresaConfigEntity = EmpresaConfigEntity(
    id = id, nomeEmpresa = nomeEmpresa, cnpj = cnpj,
    telefone = telefone, whatsapp = whatsapp, email = email,
    endereco = endereco, cidade = cidade, estado = estado,
    cep = cep, logoPath = logoPath
)
