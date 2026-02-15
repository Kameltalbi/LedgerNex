package com.ledgernex.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CompanyAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("accountId"), Index("recurrenceId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val dateEpoch: Long,          // LocalDate.toEpochDay()
    val libelle: String,
    val objet: String,
    val montantTTC: Double,
    val categorie: String,
    val accountId: Long,
    val recurrenceId: Long? = null,
    val isModified: Boolean = false
)
