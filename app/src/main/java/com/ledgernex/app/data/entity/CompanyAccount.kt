package com.ledgernex.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_accounts")
data class CompanyAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val type: AccountType,
    val soldeInitial: Double,
    val actif: Boolean = true
)
