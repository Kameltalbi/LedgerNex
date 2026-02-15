package com.ledgernex.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurrence_templates")
data class RecurrenceTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val libelle: String,
    val objet: String,
    val montantTTC: Double,
    val categorie: String,
    val accountId: Long,
    val frequence: RecurrenceFrequency,
    val dateDebutEpoch: Long,     // LocalDate.toEpochDay()
    val dateFinEpoch: Long? = null,
    val active: Boolean = true
)
