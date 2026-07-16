package com.tecmanutencao.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tecmanutencao.app.domain.model.Cliente

@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey(autoGenerate = true)
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

fun ClienteEntity.toDomain(): Cliente = Cliente(
    id = id, nomeCompleto = nomeCompleto, cpfCnpj = cpfCnpj,
    telefone = telefone, whatsapp = whatsapp, email = email,
    endereco = endereco, cidade = cidade, estado = estado,
    cep = cep, observacoes = observacoes, dataCadastro = dataCadastro
)

fun Cliente.toEntity(): ClienteEntity = ClienteEntity(
    id = id, nomeCompleto = nomeCompleto, cpfCnpj = cpfCnpj,
    telefone = telefone, whatsapp = whatsapp, email = email,
    endereco = endereco, cidade = cidade, estado = estado,
    cep = cep, observacoes = observacoes, dataCadastro = dataCadastro
)
