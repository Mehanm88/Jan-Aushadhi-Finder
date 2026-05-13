package com.example.janna.data

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class MedicineRepository(private val medicineDao: MedicineDao) {

    val allMedicines: Flow<List<Medicine>> = medicineDao.getAllMedicines()

    suspend fun getAllMedicinesList(): List<Medicine> {
        return medicineDao.getAllMedicinesList()
    }

    fun searchMedicines(query: String): Flow<List<Medicine>> {
        return medicineDao.searchMedicines("%$query%")
    }

    suspend fun insertAll(medicines: List<Medicine>) {
        medicineDao.insertAll(medicines)
    }

    companion object {
        fun generateMockStores(userLocation: LatLng): List<Store> {
            val names = listOf("Jan Aushadhi Kendra", "Pradhan Mantri Kendra", "Puri Generic Pharmacy", "PMBI Medical Store", "Seva Generic Kendra")
            val areas = listOf("Sector", "Block", "Main Road", "Cross", "Near Hospital", "Market Area", "Plaza", "Health Hub", "Extension", "Phase")
            
            return List(12) { i ->
                val latOffset = Random.nextDouble(-0.05, 0.05)
                val lngOffset = Random.nextDouble(-0.05, 0.05)
                val distance = Math.sqrt(latOffset * latOffset + lngOffset * lngOffset) * 111
                
                Store(
                    id = "store_$i",
                    name = "${names.random()} - ${areas.random()} ${Random.nextInt(1, 100)}",
                    address = "Street ${Random.nextInt(1, 200)}, City Center",
                    latitude = userLocation.latitude + latOffset,
                    longitude = userLocation.longitude + lngOffset,
                    isOpen = Random.nextBoolean(),
                    distance = "%.1f km".format(distance),
                    inventory = listOf("Paracetamol", "Pantoprazole", "Cetirizine", "Atorvastatin", "Amoxycillin").shuffled().take(Random.nextInt(2, 5)),
                    rating = 4.0f + Random.nextFloat() * 1.0f,
                    reviewCount = Random.nextInt(50, 500),
                    isStockAvailable = Random.nextBoolean()
                )
            }
        }

        // Sample data for UI components that are not yet migrated to Flow/DB
        val medicines = listOf(
            Medicine(brandName = "Dolo 650", genericName = "Paracetamol", brandPrice = 30.0, genericPrice = 12.0, category = "Analgesic", dosage = "650mg"),
            Medicine(brandName = "Pan 40", genericName = "Pantoprazole", brandPrice = 150.0, genericPrice = 45.0, category = "Gastrointestinal", dosage = "40mg"),
            Medicine(brandName = "Augmentin 625", genericName = "Amoxycillin + Potassium Clavulanate", brandPrice = 200.0, genericPrice = 80.0, category = "Antibiotic", dosage = "625mg"),
            Medicine(brandName = "Zyrtec", genericName = "Cetirizine", brandPrice = 50.0, genericPrice = 15.0, category = "Antihistamine", dosage = "10mg"),
            Medicine(brandName = "Lipitor", genericName = "Atorvastatin", brandPrice = 120.0, genericPrice = 40.0, category = "Statins", dosage = "20mg")
        )

        val stores = listOf(
            Store(
                name = "Jan Aushadhi Kendra - Majestic",
                address = "Platform 1, City Railway Station, Bengaluru",
                latitude = 12.9756,
                longitude = 77.5726,
                isOpen = true,
                distance = "0.8 km",
                inventory = listOf("Paracetamol", "Cetirizine", "Atorvastatin")
            ),
            Store(
                name = "Kendra - Malleshwaram",
                address = "15th Cross, Sampige Road, Bengaluru",
                latitude = 12.9988,
                longitude = 77.5714,
                isOpen = true,
                distance = "2.4 km",
                inventory = listOf("Pantoprazole", "Amoxycillin", "Paracetamol")
            ),
            Store(
                name = "Kendra - Jayanagar 4th Block",
                address = "Next to Bus Stand, Bengaluru",
                latitude = 12.9285,
                longitude = 77.5832,
                isOpen = false,
                distance = "5.1 km",
                inventory = listOf("Atorvastatin", "Paracetamol")
            )
        )
    }
}
