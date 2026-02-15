package com.ledgernex.app

import com.google.common.truth.Truth.assertThat
import com.ledgernex.app.data.converters.Converters
import com.ledgernex.app.data.entity.AccountType
import com.ledgernex.app.data.entity.RecurrenceFrequency
import com.ledgernex.app.data.entity.TransactionType
import org.junit.Test

/**
 * Tests unitaires pour les TypeConverters Room.
 * Vérifie que les enums sont stockées en String (name) et restaurées correctement.
 */
class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `TransactionType RECETTE round-trip`() {
        val stored = converters.fromTransactionType(TransactionType.RECETTE)
        assertThat(stored).isEqualTo("RECETTE")
        val restored = converters.toTransactionType(stored)
        assertThat(restored).isEqualTo(TransactionType.RECETTE)
    }

    @Test
    fun `TransactionType DEPENSE round-trip`() {
        val stored = converters.fromTransactionType(TransactionType.DEPENSE)
        assertThat(stored).isEqualTo("DEPENSE")
        val restored = converters.toTransactionType(stored)
        assertThat(restored).isEqualTo(TransactionType.DEPENSE)
    }

    @Test
    fun `AccountType BANK round-trip`() {
        val stored = converters.fromAccountType(AccountType.BANK)
        assertThat(stored).isEqualTo("BANK")
        val restored = converters.toAccountType(stored)
        assertThat(restored).isEqualTo(AccountType.BANK)
    }

    @Test
    fun `AccountType CASH round-trip`() {
        val stored = converters.fromAccountType(AccountType.CASH)
        assertThat(stored).isEqualTo("CASH")
        val restored = converters.toAccountType(stored)
        assertThat(restored).isEqualTo(AccountType.CASH)
    }

    @Test
    fun `RecurrenceFrequency MONTHLY round-trip`() {
        val stored = converters.fromRecurrenceFrequency(RecurrenceFrequency.MONTHLY)
        assertThat(stored).isEqualTo("MONTHLY")
        val restored = converters.toRecurrenceFrequency(stored)
        assertThat(restored).isEqualTo(RecurrenceFrequency.MONTHLY)
    }
}
