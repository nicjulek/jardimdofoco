package com.example.projetofinalmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projetofinalmobile.model.PlantState
import com.example.projetofinalmobile.model.StudySprint
import com.example.projetofinalmobile.model.UserInventoryItem
import com.example.projetofinalmobile.util.Converters // Será criado abaixo

//marca a classe como banco de dados
@Database(
    entities = [PlantState::class, StudySprint::class, UserInventoryItem::class], //todas as classes (tabelas) q fazem parte do banco
    version = 6, //versao do banco
    exportSchema = false //desabilita a exportacao do banco
)
@TypeConverters(Converters::class) //converte tipos personalizados
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun studySprintDao(): StudySprintDao
    abstract fun userInventoryDao(): UserInventoryDao

    companion object { //tipo um static
        @Volatile //ler da mem principal
        private var INSTANCE: AppDatabase? = null

        //coluna 'totalActiveMinutes' na tabela 'plant_state'
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_state ADD COLUMN totalActiveMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }

        //cria a tabela 'study_sprints'
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `study_sprints` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `durationMinutes` INTEGER NOT NULL)")
            }
        }

        //adiciona coluna 'userPoints'
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_state ADD COLUMN userPoints INTEGER NOT NULL DEFAULT 0")
            }
        }


        //cria a tabela 'user_inventory'
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_inventory` (`itemId` TEXT NOT NULL, `itemType` TEXT NOT NULL, `name` TEXT NOT NULL, `resourceId` TEXT, PRIMARY KEY(`itemId`))")
            }
        }

        //adiciona coluna 'currentPlantSkin'
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plant_state ADD COLUMN currentPlantSkin TEXT NOT NULL DEFAULT 'default_plant'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_plant_database" //nome do arquivo do banco
                )
                    //add as migrations
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}