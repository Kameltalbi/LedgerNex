package com.ledgernex.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class Asset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val dateAchatEpoch: Long,     // LocalDate.toEpochDay()
    val montantTTC: Double,       // prix unitaire
    val quantite: Int = 1,        // nombre d'unités
    val dureeAmortissement: Int   // en années
)
