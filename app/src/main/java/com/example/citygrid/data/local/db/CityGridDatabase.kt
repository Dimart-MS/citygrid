package com.example.citygrid.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.citygrid.data.local.db.dao.AguaDao
import com.example.citygrid.data.local.db.dao.AlertaDao
import com.example.citygrid.data.local.db.dao.AlumbradoDao
import com.example.citygrid.data.local.db.dao.ResiduosDao
import com.example.citygrid.data.local.db.entity.AguaEntity
import com.example.citygrid.data.local.db.entity.AlertaEntity
import com.example.citygrid.data.local.db.entity.AlumbradoEntity
import com.example.citygrid.data.local.db.entity.ResiduosEntity

@Database(
    entities = [
        ResiduosEntity::class,
        AguaEntity::class,
        AlumbradoEntity::class,
        AlertaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CityGridDatabase : RoomDatabase() {

    abstract fun residuosDao(): ResiduosDao
    abstract fun aguaDao(): AguaDao
    abstract fun alumbradoDao(): AlumbradoDao
    abstract fun alertaDao(): AlertaDao

    companion object {
        @Volatile
        private var INSTANCE: CityGridDatabase? = null

        fun getInstance(context: Context): CityGridDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CityGridDatabase::class.java,
                    "citygrid_local.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
