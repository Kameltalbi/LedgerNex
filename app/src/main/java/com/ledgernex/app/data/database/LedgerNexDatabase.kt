package com.ledgernex.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ledgernex.app.data.converters.Converters
import com.ledgernex.app.data.dao.AccountDao
import com.ledgernex.app.data.dao.AssetDao
import com.ledgernex.app.data.dao.RecurrenceDao
import com.ledgernex.app.data.dao.TransactionDao
import com.ledgernex.app.data.entity.Asset
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.entity.RecurrenceTemplate
import com.ledgernex.app.data.entity.Transaction

@Database(
    entities = [
        CompanyAccount::class,
        Transaction::class,
        RecurrenceTemplate::class,
        Asset::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LedgerNexDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurrenceDao(): RecurrenceDao
    abstract fun assetDao(): AssetDao

    companion object {
        @Volatile
        private var INSTANCE: LedgerNexDatabase? = null

        fun getInstance(context: Context): LedgerNexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LedgerNexDatabase::class.java,
                    "ledgernex_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
