package com.ledgernex.app.data.entity

data class AccountWithBalance(
    val id: Long,
    val nom: String,
    val soldeInitial: Double,
    val actif: Boolean,
    val type: AccountType,
    val balance: Double
) {
    fun toCompanyAccount() = CompanyAccount(
        id = id,
        nom = nom,
        soldeInitial = balance, // Use calculated balance
        actif = actif,
        type = type
    )
}
