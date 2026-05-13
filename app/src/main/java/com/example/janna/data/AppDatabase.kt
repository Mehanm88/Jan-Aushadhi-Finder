package com.example.janna.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Medicine::class, ReminderEntity::class, ProfileEntity::class], 
    version = 4, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medicine_table ADD COLUMN sideEffects TEXT NOT NULL DEFAULT 'No significant side effects reported.'")
                db.execSQL("ALTER TABLE medicine_table ADD COLUMN manufacturer TEXT NOT NULL DEFAULT 'Generic Labs'")
            }
        }

        // Migration from version 3 to 4 adding Profile support and stock tracking
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create profile_table
                db.execSQL("CREATE TABLE IF NOT EXISTS `profile_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `relation` TEXT NOT NULL, `avatarEmoji` TEXT NOT NULL)")
                
                // Update reminder_table
                db.execSQL("ALTER TABLE reminder_table ADD COLUMN profileId INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE reminder_table ADD COLUMN dosageTime TEXT NOT NULL DEFAULT 'Morning'")
                db.execSQL("ALTER TABLE reminder_table ADD COLUMN currentStock INTEGER NOT NULL DEFAULT 30")
                db.execSQL("ALTER TABLE reminder_table ADD COLUMN dosagePerDay INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE reminder_table ADD COLUMN lastRefillDate INTEGER NOT NULL DEFAULT 0")
                
                // Insert default profile
                db.execSQL("INSERT INTO profile_table (id, name, relation, avatarEmoji) VALUES (1, 'Main User', 'Self', '👤')")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicine_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.medicineDao())
                }
            }
        }

        suspend fun populateDatabase(medicineDao: MedicineDao) {
            val sampleMedicines = listOf(
                Medicine(brandName = "Dolo 650", genericName = "Paracetamol", brandPrice = 30.0, genericPrice = 12.0, category = "Analgesic", manufacturer = "Micro Labs"),
                Medicine(brandName = "Pan 40", genericName = "Pantoprazole", brandPrice = 150.0, genericPrice = 45.0, category = "Gastrointestinal", manufacturer = "Alkem"),
                Medicine(brandName = "Augmentin 625", genericName = "Amoxycillin + Potassium Clavulanate", brandPrice = 200.0, genericPrice = 80.0, category = "Antibiotic", manufacturer = "GSK"),
                Medicine(brandName = "Zyrtec", genericName = "Cetirizine", brandPrice = 50.0, genericPrice = 15.0, category = "Antihistamine", manufacturer = "Dr. Reddys"),
                Medicine(brandName = "Lipitor", genericName = "Atorvastatin", brandPrice = 120.0, genericPrice = 40.0, category = "Statins", manufacturer = "Pfizer")
            )
            medicineDao.insertAll(sampleMedicines)
        }
    }
}
