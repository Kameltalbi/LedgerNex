package com.ledgernex.app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.ledgernex.app.data.database.LedgerNexDatabase
import com.ledgernex.app.data.datastore.SettingsDataStore
import com.ledgernex.app.data.repository.AccountRepositoryImpl
import com.ledgernex.app.data.repository.AssetRepositoryImpl
import com.ledgernex.app.data.repository.RecurrenceRepositoryImpl
import com.ledgernex.app.data.repository.TransactionRepositoryImpl
import com.ledgernex.app.domain.repository.AccountRepository
import com.ledgernex.app.domain.repository.AssetRepository
import com.ledgernex.app.domain.repository.RecurrenceRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import com.ledgernex.app.manager.RecurrenceManager
import com.ledgernex.app.data.sync.CloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Application class – point d'entrée pour l'initialisation des dépendances.
 * En V1 on utilise un service locator simple. Évolutif vers Hilt/Koin en V2.
 */
class LedgerNexApp : Application() {

    lateinit var database: LedgerNexDatabase
        private set

    lateinit var accountRepository: AccountRepository
        private set

    lateinit var transactionRepository: TransactionRepository
        private set

    lateinit var recurrenceRepository: RecurrenceRepository
        private set

    lateinit var assetRepository: AssetRepository
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    lateinit var recurrenceManager: RecurrenceManager
        private set

    lateinit var cloudSyncManager: CloudSyncManager
        private set

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase manually with configuration
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:846871498556:android:c4541eebaa5fcb6c759839")
                    .setProjectId("ledgernex")
                    .setApiKey("***REMOVED***")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Log.e("LedgerNexApp", "Firebase init error: ${e.message}")
        }

        database = LedgerNexDatabase.getInstance(this)

        accountRepository = AccountRepositoryImpl(database.accountDao())
        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        recurrenceRepository = RecurrenceRepositoryImpl(database.recurrenceDao())
        assetRepository = AssetRepositoryImpl(database.assetDao())

        settingsDataStore = SettingsDataStore(this)

        recurrenceManager = RecurrenceManager(recurrenceRepository, transactionRepository)
        
        // Cloud Sync Manager
        cloudSyncManager = CloudSyncManager(transactionRepository, accountRepository, assetRepository)
    }
}
