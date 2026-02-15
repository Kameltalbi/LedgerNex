package com.ledgernex.app.data.converters

import androidx.room.TypeConverter
import com.ledgernex.app.data.entity.AccountType
import com.ledgernex.app.data.entity.RecurrenceFrequency
import com.ledgernex.app.data.entity.TransactionType

/**
 * TypeConverters pour Room.
 * Toutes les enums sont stockées en String (name), jamais en ordinal.
 */
class Converters {

    // --- TransactionType ---
    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    // --- AccountType ---
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    // --- RecurrenceFrequency ---
    @TypeConverter
    fun fromRecurrenceFrequency(value: RecurrenceFrequency): String = value.name

    @TypeConverter
    fun toRecurrenceFrequency(value: String): RecurrenceFrequency = RecurrenceFrequency.valueOf(value)
}
