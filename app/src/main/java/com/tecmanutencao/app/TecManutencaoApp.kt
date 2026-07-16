package com.tecmanutencao.app

import android.app.Application
import com.tecmanutencao.app.data.database.AppDatabase
import com.tecmanutencao.app.data.repository.ClienteRepository
import com.tecmanutencao.app.data.repository.EmpresaConfigRepository
import com.tecmanutencao.app.data.repository.EquipamentoRepository
import com.tecmanutencao.app.data.repository.OrcamentoRepository

class TecManutencaoApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var clienteRepository: ClienteRepository
        private set

    lateinit var equipamentoRepository: EquipamentoRepository
        private set

    lateinit var orcamentoRepository: OrcamentoRepository
        private set

    lateinit var empresaConfigRepository: EmpresaConfigRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)

        clienteRepository = ClienteRepository(database.clienteDao())
        equipamentoRepository = EquipamentoRepository(database.equipamentoDao())
        orcamentoRepository = OrcamentoRepository(database.orcamentoDao())
        empresaConfigRepository = EmpresaConfigRepository(database.empresaConfigDao())
    }
}
