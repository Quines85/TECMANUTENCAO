package com.tecmanutencao.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tecmanutencao.app.data.database.dao.ClienteDao
import com.tecmanutencao.app.data.database.dao.EmpresaConfigDao
import com.tecmanutencao.app.data.database.dao.EquipamentoDao
import com.tecmanutencao.app.data.database.dao.OrcamentoDao
import com.tecmanutencao.app.data.database.entity.ClienteEntity
import com.tecmanutencao.app.data.database.entity.EmpresaConfigEntity
import com.tecmanutencao.app.data.database.entity.EquipamentoEntity
import com.tecmanutencao.app.data.database.entity.OrcamentoEntity

@Database(
    entities = [
        ClienteEntity::class,
        EquipamentoEntity::class,
        OrcamentoEntity::class,
        EmpresaConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun equipamentoDao(): EquipamentoDao
    abstract fun orcamentoDao(): OrcamentoDao
    abstract fun empresaConfigDao(): EmpresaConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tec_manutencao_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
