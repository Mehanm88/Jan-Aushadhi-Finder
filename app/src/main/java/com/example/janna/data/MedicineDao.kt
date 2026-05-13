package com.example.janna.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicine_table")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicine_table")
    suspend fun getAllMedicinesList(): List<Medicine>

    @Query("SELECT * FROM medicine_table WHERE brandName LIKE :query OR genericName LIKE :query")
    fun searchMedicines(query: String): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<Medicine>)
}
