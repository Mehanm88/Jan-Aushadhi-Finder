package com.example.janna.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val medicineCollection = db.collection("medicines")

    suspend fun saveMedicine(medicine: Medicine) {
        try {
            medicineCollection.document(medicine.id.toString()).set(medicine).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllMedicines(): List<Medicine> {
        return try {
            val snapshot = medicineCollection.get().await()
            snapshot.toObjects(Medicine::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun syncLocalToCloud(localMedicines: List<Medicine>) {
        localMedicines.forEach { saveMedicine(it) }
    }
}
